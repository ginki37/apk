package com.ai3dstudio.mobile.core.network

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Dynamic API surface driven entirely by the discovered Base URL.
 * Every call takes a fully-qualified @Url so the same Retrofit instance can
 * target whatever paths the discovery step found on the user's server,
 * without ever asking the user to type an endpoint manually.
 */
interface ApiService {

    @GET
    suspend fun probeGet(@Url url: String): Response<okhttp3.ResponseBody>

    @POST
    suspend fun chatCompletion(@Url url: String, @Body request: ChatRequestDto): ChatResponseDto

    @POST
    suspend fun generateImage(@Url url: String, @Body request: ImageGenerationRequestDto): ImageGenerationResponseDto

    @POST
    suspend fun generateModel3d(@Url url: String, @Body request: Model3dGenerationRequestDto): Model3dGenerationResponseDto

    @POST
    suspend fun generateGame(@Url url: String, @Body request: GameGenerationRequestDto): GameGenerationResponseDto
}

@Serializable
data class ChatMessageDto(val role: String, val content: String)

@Serializable
data class ChatRequestDto(
    val model: String? = null,
    val messages: List<ChatMessageDto>,
    val stream: Boolean = false
)

@Serializable
data class ChatResponseDto(
    val id: String? = null,
    val model: String? = null,
    val reply: String? = null,
    val content: String? = null
) {
    val text: String get() = reply ?: content.orEmpty()
}

@Serializable
data class ImageGenerationRequestDto(
    val prompt: String,
    val model: String? = null,
    val width: Int = 1024,
    val height: Int = 1024,
    val count: Int = 1,
    val style: String? = null,
    val format: String = "png"
)

@Serializable
data class ImageGenerationResponseDto(
    val images: List<String> = emptyList(),
    val format: String = "png"
)

@Serializable
data class Model3dGenerationRequestDto(
    val prompt: String,
    val model: String? = null,
    val format: String = "glb",
    val detailLevel: String = "medium",
    val targetPolyCount: Int = 20000,
    val generateTextures: Boolean = true,
    val generateAnimation: Boolean = false
)

@Serializable
data class Model3dGenerationResponseDto(
    val downloadUrl: String? = null,
    val base64Data: String? = null,
    val format: String = "glb",
    val polyCount: Int? = null
)

@Serializable
data class GameGenerationRequestDto(
    val prompt: String,
    val genre: String? = null,
    val model: String? = null
)

@Serializable
data class GameGenerationResponseDto(
    val downloadUrl: String? = null,
    val base64Zip: String? = null,
    val manifest: Map<String, String> = emptyMap()
)
