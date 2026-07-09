package com.ai3dstudio.mobile.core.data.repository

import com.ai3dstudio.mobile.core.domain.model.GeneratedImage
import com.ai3dstudio.mobile.core.domain.model.ImageFormat
import com.ai3dstudio.mobile.core.domain.repository.ImageGenerationRepository
import com.ai3dstudio.mobile.core.network.ApiService
import com.ai3dstudio.mobile.core.network.ImageGenerationRequestDto
import com.ai3dstudio.mobile.core.security.RequestRateLimiter
import com.ai3dstudio.mobile.core.security.RequestValidator
import com.ai3dstudio.mobile.core.util.FileStorageManager
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageGenerationRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val serverRepositoryImpl: ServerRepositoryImpl,
    private val fileStorageManager: FileStorageManager,
    private val rateLimiter: RequestRateLimiter
) : ImageGenerationRepository {

    override suspend fun generateImages(
        prompt: String,
        width: Int,
        height: Int,
        count: Int,
        style: String?,
        format: ImageFormat
    ): Result<List<GeneratedImage>> {
        val sanitized = RequestValidator.sanitizePrompt(prompt)
        if (!RequestValidator.isValidPrompt(sanitized)) {
            return Result.failure(IllegalArgumentException("الرجاء وصف الصورة أولاً"))
        }
        val baseUrl = serverRepositoryImpl.getActiveBaseUrl()
            ?: return Result.failure(IllegalStateException("لم يتم الاتصال بخادم بعد"))

        return try {
            val response = rateLimiter.withRateLimit {
                apiService.generateImage(
                    url = "$baseUrl/images/generations",
                    request = ImageGenerationRequestDto(
                        prompt = sanitized,
                        width = width,
                        height = height,
                        count = count.coerceIn(1, 8),
                        style = style,
                        format = format.name.lowercase()
                    )
                )
            }
            val extension = format.name.lowercase()
            val images = response.images.map { base64 ->
                val path = fileStorageManager.saveImageFromBase64(base64, extension)
                GeneratedImage(
                    id = UUID.randomUUID().toString(),
                    prompt = sanitized,
                    localPath = path,
                    format = format,
                    width = width,
                    height = height,
                    style = style,
                    createdAt = System.currentTimeMillis()
                )
            }
            if (images.isEmpty()) {
                Result.failure(IllegalStateException("لم يُرجع الخادم أي صور"))
            } else {
                Result.success(images)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
