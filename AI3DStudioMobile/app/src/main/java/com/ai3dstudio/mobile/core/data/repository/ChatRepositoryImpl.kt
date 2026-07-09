package com.ai3dstudio.mobile.core.data.repository

import com.ai3dstudio.mobile.core.data.local.dao.ChatMessageDao
import com.ai3dstudio.mobile.core.data.local.entity.ChatMessageEntity
import com.ai3dstudio.mobile.core.domain.model.ChatMessage
import com.ai3dstudio.mobile.core.domain.model.ChatRole
import com.ai3dstudio.mobile.core.domain.repository.ChatRepository
import com.ai3dstudio.mobile.core.network.ApiService
import com.ai3dstudio.mobile.core.network.ChatMessageDto
import com.ai3dstudio.mobile.core.network.ChatRequestDto
import com.ai3dstudio.mobile.core.security.RequestRateLimiter
import com.ai3dstudio.mobile.core.security.RequestValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
    private val apiService: ApiService,
    private val serverRepositoryImpl: ServerRepositoryImpl,
    private val rateLimiter: RequestRateLimiter
) : ChatRepository {

    override fun observeMessages(projectId: String): Flow<List<ChatMessage>> =
        chatMessageDao.observeForProject(projectId).map { list -> list.map { it.toDomain() } }

    override suspend fun sendMessage(projectId: String, prompt: String): Result<ChatMessage> {
        val sanitized = RequestValidator.sanitizePrompt(prompt)
        if (!RequestValidator.isValidPrompt(sanitized)) {
            return Result.failure(IllegalArgumentException("الرجاء إدخال طلب صالح"))
        }

        val baseUrl = serverRepositoryImpl.getActiveBaseUrl()
            ?: return Result.failure(IllegalStateException("لم يتم الاتصال بخادم بعد"))

        val now = System.currentTimeMillis()
        val userMessage = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            role = ChatRole.USER.name,
            content = sanitized,
            attachmentPath = null,
            attachmentType = null,
            createdAt = now
        )
        chatMessageDao.insert(userMessage)

        return try {
            val history = chatMessageDao.observeForProject(projectId).first().map {
                ChatMessageDto(role = it.role.lowercase(), content = it.content)
            }
            val response = rateLimiter.withRateLimit {
                apiService.chatCompletion(
                    url = "$baseUrl/chat/completions",
                    request = ChatRequestDto(messages = history)
                )
            }
            val assistantMessage = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                role = ChatRole.ASSISTANT.name,
                content = response.text.ifBlank { "تعذر الحصول على رد من الخادم" },
                attachmentPath = null,
                attachmentType = null,
                createdAt = System.currentTimeMillis()
            )
            chatMessageDao.insert(assistantMessage)
            Result.success(assistantMessage.toDomain())
        } catch (e: Exception) {
            val errorMessage = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                role = ChatRole.SYSTEM.name,
                content = "تعذر الاتصال بالخادم: ${e.message ?: "خطأ غير معروف"}",
                attachmentPath = null,
                attachmentType = null,
                createdAt = System.currentTimeMillis()
            )
            chatMessageDao.insert(errorMessage)
            Result.failure(e)
        }
    }

    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id = id,
        projectId = projectId,
        role = ChatRole.valueOf(role),
        content = content,
        attachmentPath = attachmentPath,
        attachmentType = attachmentType?.let { com.ai3dstudio.mobile.core.domain.model.AttachmentType.valueOf(it) },
        createdAt = createdAt
    )
}
