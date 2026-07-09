package com.ai3dstudio.mobile.core.data.repository

import com.ai3dstudio.mobile.core.domain.model.GameGenre
import com.ai3dstudio.mobile.core.domain.model.GeneratedGame
import com.ai3dstudio.mobile.core.domain.repository.GameGenerationRepository
import com.ai3dstudio.mobile.core.network.ApiService
import com.ai3dstudio.mobile.core.network.GameGenerationRequestDto
import com.ai3dstudio.mobile.core.security.RequestRateLimiter
import com.ai3dstudio.mobile.core.security.RequestValidator
import com.ai3dstudio.mobile.core.util.FileStorageManager
import com.ai3dstudio.mobile.core.util.GameProjectPackager
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameGenerationRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val serverRepositoryImpl: ServerRepositoryImpl,
    private val fileStorageManager: FileStorageManager,
    private val gameProjectPackager: GameProjectPackager,
    private val rateLimiter: RequestRateLimiter
) : GameGenerationRepository {

    override suspend fun generateGame(prompt: String, genre: GameGenre): Result<GeneratedGame> {
        val sanitized = RequestValidator.sanitizePrompt(prompt)
        if (!RequestValidator.isValidPrompt(sanitized)) {
            return Result.failure(IllegalArgumentException("الرجاء وصف اللعبة أولاً"))
        }
        val baseUrl = serverRepositoryImpl.getActiveBaseUrl()
            ?: return Result.failure(IllegalStateException("لم يتم الاتصال بخادم بعد"))

        return try {
            val response = rateLimiter.withRateLimit {
                apiService.generateGame(
                    url = "$baseUrl/games/generations",
                    request = GameGenerationRequestDto(prompt = sanitized, genre = genre.name.lowercase())
                )
            }
            val zipPath = when {
                response.base64Zip != null -> fileStorageManager.saveGameZipFromBase64(response.base64Zip)
                response.manifest.isNotEmpty() ->
                    gameProjectPackager.packageGame(genre.name.lowercase(), sanitized, response.manifest)
                else -> return Result.failure(
                    IllegalStateException("لم يُرجع الخادم أي بيانات للعبة (لا ملف مضغوط ولا بيان أصول)")
                )
            }
            Result.success(
                GeneratedGame(
                    id = UUID.randomUUID().toString(),
                    prompt = sanitized,
                    genre = genre,
                    zipLocalPath = zipPath,
                    createdAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
