package com.ai3dstudio.mobile.core.domain.repository

import com.ai3dstudio.mobile.core.domain.model.ChatMessage
import com.ai3dstudio.mobile.core.domain.model.Generated3dModel
import com.ai3dstudio.mobile.core.domain.model.GeneratedGame
import com.ai3dstudio.mobile.core.domain.model.GeneratedImage
import com.ai3dstudio.mobile.core.domain.model.LibraryAsset
import com.ai3dstudio.mobile.core.domain.model.Project
import com.ai3dstudio.mobile.core.domain.model.ServerCapabilities
import kotlinx.coroutines.flow.Flow

interface ServerRepository {
    fun observeActiveServer(): Flow<ServerCapabilities?>
    suspend fun connectAndDiscover(baseUrl: String): Result<ServerCapabilities>
    suspend fun getActiveBaseUrl(): String?
    suspend fun clearServer()
}

interface ProjectRepository {
    fun observeProjects(): Flow<List<Project>>
    suspend fun getProject(id: String): Project?
    suspend fun createProject(title: String, type: com.ai3dstudio.mobile.core.domain.model.ProjectType): Project
    suspend fun deleteProject(id: String)
}

interface ChatRepository {
    fun observeMessages(projectId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(projectId: String, prompt: String): Result<ChatMessage>
}

interface ImageGenerationRepository {
    suspend fun generateImages(
        prompt: String,
        width: Int,
        height: Int,
        count: Int,
        style: String?,
        format: com.ai3dstudio.mobile.core.domain.model.ImageFormat
    ): Result<List<GeneratedImage>>
}

interface Model3dGenerationRepository {
    suspend fun generateModel(
        prompt: String,
        format: com.ai3dstudio.mobile.core.domain.model.Model3dFormat,
        detailLevel: com.ai3dstudio.mobile.core.domain.model.DetailLevel,
        targetPolyCount: Int,
        generateTextures: Boolean,
        generateAnimation: Boolean
    ): Result<Generated3dModel>
}

interface GameGenerationRepository {
    suspend fun generateGame(prompt: String, genre: com.ai3dstudio.mobile.core.domain.model.GameGenre): Result<GeneratedGame>
}

interface AssetRepository {
    fun observeAssets(): Flow<List<LibraryAsset>>
    fun searchAssets(query: String): Flow<List<LibraryAsset>>
    fun observeByCategory(category: com.ai3dstudio.mobile.core.domain.model.AssetCategory): Flow<List<LibraryAsset>>
    suspend fun addAsset(asset: LibraryAsset)
    suspend fun deleteAsset(id: String)
}
