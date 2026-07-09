package com.ai3dstudio.mobile.core.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ai3dstudio.mobile.core.data.local.dao.AssetDao
import com.ai3dstudio.mobile.core.data.local.dao.ChatMessageDao
import com.ai3dstudio.mobile.core.data.local.dao.ProjectDao
import com.ai3dstudio.mobile.core.data.local.dao.ServerProfileDao
import com.ai3dstudio.mobile.core.data.local.entity.AssetEntity
import com.ai3dstudio.mobile.core.data.local.entity.ChatMessageEntity
import com.ai3dstudio.mobile.core.data.local.entity.ProjectEntity
import com.ai3dstudio.mobile.core.data.local.entity.ServerProfileEntity

@Database(
    entities = [
        ProjectEntity::class,
        ChatMessageEntity::class,
        AssetEntity::class,
        ServerProfileEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AiStudioDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun assetDao(): AssetDao
    abstract fun serverProfileDao(): ServerProfileDao

    companion object {
        const val DATABASE_NAME = "ai3dstudio.db"
    }
}
