package com.ai3dstudio.mobile.core.network

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Probes a user-supplied Base URL against a set of well-known discovery
 * endpoints and merges whatever the server exposes into a single
 * CapabilityManifest. This is the entire "zero-config" mechanism described
 * in the product spec: the user only ever types a Base URL.
 */
@Singleton
class CapabilityDiscoveryService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val discoveryPaths = listOf(
        "models", "v1/models", "capabilities", "v1/capabilities",
        "health", "v1/health", "limits", "rate_limits", "info", "v1/info"
    )

    suspend fun discover(baseUrl: String): CapabilityManifest = coroutineScope {
        val normalizedBase = baseUrl.trimEnd('/')
        val deferredResponses = discoveryPaths.map { path ->
            async { path to safeGet("$normalizedBase/$path") }
        }
        val results = deferredResponses.awaitAll2()

        var manifest = CapabilityManifest()
        for ((path, body) in results) {
            if (body == null) continue
            manifest = mergeIntoManifest(manifest, path, body)
        }
        manifest
    }

    private suspend fun List<kotlinx.coroutines.Deferred<Pair<String, String?>>>.awaitAll2() =
        this.map { it.await() }

    private fun safeGet(url: String): String? = try {
        val request = Request.Builder().url(url).get().build()
        okHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) response.body?.string() else null
        }
    } catch (_: Exception) {
        null
    }

    private fun mergeIntoManifest(current: CapabilityManifest, path: String, rawBody: String): CapabilityManifest {
        val element = try {
            json.parseToJsonElement(rawBody)
        } catch (_: Exception) {
            return current
        }
        if (element !is JsonObject) return current

        var manifest = current

        extractModelsArray(element)?.let { models ->
            manifest = manifest.copy(
                models = (manifest.models + models).distinctBy { it.id },
                chatCapable = manifest.chatCapable || models.any { it.supportsChat },
                imageCapable = manifest.imageCapable || models.any { it.supportsImage },
                model3dCapable = manifest.model3dCapable || models.any { it.supportsModel3d },
                gameCapable = manifest.gameCapable || models.any { it.supportsGame }
            )
        }

        element["name"]?.jsonPrimitive?.content?.let {
            manifest = manifest.copy(server = manifest.server.copy(name = it))
        }
        element["version"]?.jsonPrimitive?.content?.let {
            manifest = manifest.copy(server = manifest.server.copy(version = it))
        }
        element["provider"]?.jsonPrimitive?.content?.let {
            manifest = manifest.copy(server = manifest.server.copy(provider = it))
        }

        (element["chat"] ?: element["supports_chat"])?.jsonPrimitive?.content?.toBoolean()?.let {
            manifest = manifest.copy(chatCapable = manifest.chatCapable || it)
        }
        (element["image"] ?: element["supports_image"])?.jsonPrimitive?.content?.toBoolean()?.let {
            manifest = manifest.copy(imageCapable = manifest.imageCapable || it)
        }
        (element["model3d"] ?: element["supports_3d"])?.jsonPrimitive?.content?.toBoolean()?.let {
            manifest = manifest.copy(model3dCapable = manifest.model3dCapable || it)
        }
        (element["game"] ?: element["supports_game"])?.jsonPrimitive?.content?.toBoolean()?.let {
            manifest = manifest.copy(gameCapable = manifest.gameCapable || it)
        }

        val requestsPerMinute = (element["requests_per_minute"] ?: element["rpm"])
            ?.jsonPrimitive?.content?.toIntOrNull()
        val requestsPerDay = (element["requests_per_day"] ?: element["rpd"])
            ?.jsonPrimitive?.content?.toIntOrNull()
        val remaining = element["remaining"]?.jsonPrimitive?.content?.toIntOrNull()
        if (requestsPerMinute != null || requestsPerDay != null || remaining != null) {
            manifest = manifest.copy(
                rateLimit = RateLimitInfo(
                    requestsPerMinute = requestsPerMinute ?: manifest.rateLimit.requestsPerMinute,
                    requestsPerDay = requestsPerDay ?: manifest.rateLimit.requestsPerDay,
                    remaining = remaining ?: manifest.rateLimit.remaining
                )
            )
        }

        // Path itself is a capability signal even without a rich schema.
        when {
            path.contains("model") -> manifest = manifest.copy(chatCapable = true)
        }

        return manifest
    }

    private fun extractModelsArray(element: JsonObject): List<ModelInfo>? {
        val array: JsonArray = when {
            element["models"] is JsonArray -> element["models"]!!.jsonArray
            element["data"] is JsonArray -> element["data"]!!.jsonArray
            else -> return null
        }
        return array.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            val id = (obj["id"] ?: obj["name"])?.jsonPrimitive?.content ?: return@mapNotNull null
            val capabilities = obj["capabilities"]?.jsonArray?.map { (it as? JsonPrimitive)?.content.orEmpty() }
                ?: emptyList()
            ModelInfo(
                id = id,
                displayName = obj["display_name"]?.jsonPrimitive?.content ?: id,
                supportsChat = capabilities.any { it.contains("chat") || it.contains("text") } || capabilities.isEmpty(),
                supportsImage = capabilities.any { it.contains("image") || it.contains("vision") },
                supportsModel3d = capabilities.any { it.contains("3d") || it.contains("mesh") },
                supportsGame = capabilities.any { it.contains("game") }
            )
        }
    }
}
