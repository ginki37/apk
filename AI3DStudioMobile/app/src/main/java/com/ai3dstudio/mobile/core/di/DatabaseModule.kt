package com.ai3dstudio.mobile.core.di

import android.content.Context
import androidx.room.Room
import com.ai3dstudio.mobile.core.data.local.dao.AssetDao
import com.ai3dstudio.mobile.core.data.local.dao.ChatMessageDao
import com.ai3dstudio.mobile.core.data.local.dao.ProjectDao
import com.ai3dstudio.mobile.core.data.local.dao.ServerProfileDao
import com.ai3dstudio.mobile.core.data.local.db.AiStudioDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AiStudioDatabase =
        Room.databaseBuilder(context, AiStudioDatabase::class.java, AiStudioDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideProjectDao(db: AiStudioDatabase): ProjectDao = db.projectDao()

    @Provides
    fun provideChatMessageDao(db: AiStudioDatabase): ChatMessageDao = db.chatMessageDao()

    @Provides
    fun provideAssetDao(db: AiStudioDatabase): AssetDao = db.assetDao()

    @Provides
    fun provideServerProfileDao(db: AiStudioDatabase): ServerProfileDao = db.serverProfileDao()
}
