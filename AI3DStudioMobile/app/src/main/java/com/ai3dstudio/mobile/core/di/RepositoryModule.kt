package com.ai3dstudio.mobile.core.di

import com.ai3dstudio.mobile.core.data.repository.AssetRepositoryImpl
import com.ai3dstudio.mobile.core.data.repository.ChatRepositoryImpl
import com.ai3dstudio.mobile.core.data.repository.GameGenerationRepositoryImpl
import com.ai3dstudio.mobile.core.data.repository.ImageGenerationRepositoryImpl
import com.ai3dstudio.mobile.core.data.repository.Model3dGenerationRepositoryImpl
import com.ai3dstudio.mobile.core.data.repository.ProjectRepositoryImpl
import com.ai3dstudio.mobile.core.data.repository.ServerRepositoryImpl
import com.ai3dstudio.mobile.core.domain.repository.AssetRepository
import com.ai3dstudio.mobile.core.domain.repository.ChatRepository
import com.ai3dstudio.mobile.core.domain.repository.GameGenerationRepository
import com.ai3dstudio.mobile.core.domain.repository.ImageGenerationRepository
import com.ai3dstudio.mobile.core.domain.repository.Model3dGenerationRepository
import com.ai3dstudio.mobile.core.domain.repository.ProjectRepository
import com.ai3dstudio.mobile.core.domain.repository.ServerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindServerRepository(impl: ServerRepositoryImpl): ServerRepository

    @Binds
    @Singleton
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindImageGenerationRepository(impl: ImageGenerationRepositoryImpl): ImageGenerationRepository

    @Binds
    @Singleton
    abstract fun bindModel3dGenerationRepository(impl: Model3dGenerationRepositoryImpl): Model3dGenerationRepository

    @Binds
    @Singleton
    abstract fun bindGameGenerationRepository(impl: GameGenerationRepositoryImpl): GameGenerationRepository

    @Binds
    @Singleton
    abstract fun bindAssetRepository(impl: AssetRepositoryImpl): AssetRepository
}
