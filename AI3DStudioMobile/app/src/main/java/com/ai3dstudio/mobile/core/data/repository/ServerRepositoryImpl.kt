package com.ai3dstudio.mobile.core.data.repository

import com.ai3dstudio.mobile.core.data.local.dao.ServerProfileDao
import com.ai3dstudio.mobile.core.data.local.entity.ServerProfileEntity
import com.ai3dstudio.mobile.core.domain.model.ServerCapabilities
import com.ai3dstudio.mobile.core.domain.repository.ServerRepository
import com.ai3dstudio.mobile.core.network.CapabilityDiscoveryService
import com.ai3dstudio.mobile.core.network.CapabilityManifest
import com.ai3dstudio.mobile.core.security.RequestValidator
import com.ai3dstudio.mobile.core.security.SecureCredentialStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepositoryImpl @Inject constructor(
    private val discoveryService: CapabilityDiscoveryService,
    private val serverProfileDao: ServerProfileDao,
    private val credentialStore: SecureCredentialStore
) : ServerRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observeActiveServer(): Flow<ServerCapabilities?> =
        serverProfileDao.observeActive().map { entity -> entity?.toDomain(json) }

    override suspend fun connectAndDiscover(baseUrl: String): Result<ServerCapabilities> {
        val trimmed = baseUrl.trim().trimEnd('/')
        if (!RequestValidator.isValidBaseUrl(trimmed)) {
            return Result.failure(IllegalArgumentException("عنوان الخادم غير صالح"))
        }
        return try {
            val manifest = discoveryService.discover(trimmed)
            serverProfileDao.deactivateAll()
            val entity = ServerProfileEntity(
                baseUrl = trimmed,
                serverName = manifest.server.name,
                serverVersion = manifest.server.version,
                capabilitiesJson = json.encodeToString(manifest),
                lastDiscoveredAt = System.currentTimeMillis(),
                isActive = true
            )
            serverProfileDao.upsert(entity)
            credentialStore.saveBaseUrl(trimmed)
            Result.success(entity.toDomain(json))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActiveBaseUrl(): String? =
        serverProfileDao.getActive()?.baseUrl ?: credentialStore.getBaseUrl()

    override suspend fun clearServer() {
        serverProfileDao.deactivateAll()
        credentialStore.clearAll()
    }

    private fun ServerProfileEntity.toDomain(json: Json): ServerCapabilities {
        val manifest = try {
            json.decodeFromString<CapabilityManifest>(capabilitiesJson)
        } catch (_: Exception) {
            CapabilityManifest()
        }
        return ServerCapabilities(
            baseUrl = baseUrl,
            serverName = serverName,
            serverVersion = serverVersion,
            availableModels = manifest.models.map { it.id },
            chatCapable = manifest.chatCapable,
            imageCapable = manifest.imageCapable,
            model3dCapable = manifest.model3dCapable,
            gameCapable = manifest.gameCapable,
            requestsPerMinute = manifest.rateLimit.requestsPerMinute,
            requestsPerDay = manifest.rateLimit.requestsPerDay
        )
    }
}
