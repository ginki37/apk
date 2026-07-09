package com.ai3dstudio.mobile.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val role: String,
    val content: String,
    val attachmentPath: String?,
    val attachmentType: String?,
    val createdAt: Long
)
