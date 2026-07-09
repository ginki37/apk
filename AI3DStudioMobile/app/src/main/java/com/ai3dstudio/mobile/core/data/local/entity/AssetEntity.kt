package com.ai3dstudio.mobile.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val id: String,
    val projectId: String?,
    val name: String,
    val category: String,
    val filePath: String,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAt: Long,
    val metadataJson: String
)
