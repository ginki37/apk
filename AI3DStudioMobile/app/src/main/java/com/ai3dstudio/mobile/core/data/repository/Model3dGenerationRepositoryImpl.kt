package com.ai3dstudio.mobile.core.data.repository

import com.ai3dstudio.mobile.core.domain.model.DetailLevel
import com.ai3dstudio.mobile.core.domain.model.Generated3dModel
import com.ai3dstudio.mobile.core.domain.model.Model3dFormat
import com.ai3dstudio.mobile.core.domain.repository.Model3dGenerationRepository
import com.ai3dstudio.mobile.core.network.ApiService
import com.ai3dstudio.mobile.core.network.Model3dGenerationRequestDto
import com.ai3dstudio.mobile.core.security.RequestRateLimiter
import com.ai3dstudio.mobile.core.security.RequestValidator
import com.ai3dstudio.mobile.core.util.FileStorageManager
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Model3dGenerationRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val serverRepositoryImpl: ServerRepositoryImpl,
    private val fileStorageManager: FileStorageManager,
    private val rateLimiter: RequestRateLimiter
) : Model3dGenerationRepository {

    override suspend fun generateModel(
        prompt: String,
        format: Model3dFormat,
        detailLevel: DetailLevel,
        targetPolyCount: Int,
        generateTextures: Boolean,
        generateAnimation: Boolean
    ): Result<Generated3dModel> {
        val sanitized = RequestValidator.sanitizePrompt(prompt)
        if (!RequestValidator.isValidPrompt(sanitized)) {
            return Result.failure(IllegalArgumentException("الرجاء وصف النموذج ثلاثي الأبعاد أولاً"))
        }
        val baseUrl = serverRepositoryImpl.getActiveBaseUrl()
            ?: return Result.failure(IllegalStateException("لم يتم الاتصال بخادم بعد"))

        return try {
            val response = rateLimiter.withRateLimit {
                apiService.generateModel3d(
                    url = "$baseUrl/models3d/generations",
                    request = Model3dGenerationRequestDto(
                        prompt = sanitized,
                        format = format.name.lowercase(),
                        detailLevel = detailLevel.name.lowercase(),
                        targetPolyCount = targetPolyCount,
                        generateTextures = generateTextures,
                        generateAnimation = generateAnimation
                    )
                )
            }
            val base64 = response.base64Data
                ?: return Result.failure(IllegalStateException("لم يُرجع الخادم بيانات النموذج"))
            val path = fileStorageManager.saveModelFromBase64(base64, format.name.lowercase())
            Result.success(
                Generated3dModel(
                    id = UUID.randomUUID().toString(),
                    prompt = sanitized,
                    localPath = path,
                    format = format,
                    detailLevel = detailLevel,
                    polyCount = response.polyCount ?: targetPolyCount,
                    hasTextures = generateTextures,
                    hasAnimation = generateAnimation,
                    createdAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
