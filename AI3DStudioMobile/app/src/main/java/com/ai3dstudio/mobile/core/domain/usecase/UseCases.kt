package com.ai3dstudio.mobile.core.domain.usecase

import com.ai3dstudio.mobile.core.domain.model.ChatMessage
import com.ai3dstudio.mobile.core.domain.model.DetailLevel
import com.ai3dstudio.mobile.core.domain.model.Generated3dModel
import com.ai3dstudio.mobile.core.domain.model.GameGenre
import com.ai3dstudio.mobile.core.domain.model.GeneratedGame
import com.ai3dstudio.mobile.core.domain.model.GeneratedImage
import com.ai3dstudio.mobile.core.domain.model.ImageFormat
import com.ai3dstudio.mobile.core.domain.model.Model3dFormat
import com.ai3dstudio.mobile.core.domain.model.Project
import com.ai3dstudio.mobile.core.domain.model.ProjectType
import com.ai3dstudio.mobile.core.domain.model.ServerCapabilities
import com.ai3dstudio.mobile.core.domain.repository.AssetRepository
import com.ai3dstudio.mobile.core.domain.repository.ChatRepository
import com.ai3dstudio.mobile.core.domain.repository.GameGenerationRepository
import com.ai3dstudio.mobile.core.domain.repository.ImageGenerationRepository
import com.ai3dstudio.mobile.core.domain.repository.Model3dGenerationRepository
import com.ai3dstudio.mobile.core.domain.repository.ProjectRepository
import com.ai3dstudio.mobile.core.domain.repository.ServerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConnectToServerUseCase @Inject constructor(
    private val serverRepository: ServerRepository
) {
    suspend operator fun invoke(baseUrl: String): Result<ServerCapabilities> =
        serverRepository.connectAndDiscover(baseUrl)
}

class ObserveActiveServerUseCase @Inject constructor(
    private val serverRepository: ServerRepository
) {
    operator fun invoke(): Flow<ServerCapabilities?> = serverRepository.observeActiveServer()
}

class ObserveProjectsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    operator fun invoke(): Flow<List<Project>> = projectRepository.observeProjects()
}

class CreateProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(title: String, type: ProjectType): Project =
        projectRepository.createProject(title, type)
}

class DeleteProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(id: String) = projectRepository.deleteProject(id)
}

class ObserveChatMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(projectId: String): Flow<List<ChatMessage>> = chatRepository.observeMessages(projectId)
}

class SendChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(projectId: String, prompt: String): Result<ChatMessage> =
        chatRepository.sendMessage(projectId, prompt)
}

class GenerateImagesUseCase @Inject constructor(
    private val imageGenerationRepository: ImageGenerationRepository
) {
    suspend operator fun invoke(
        prompt: String,
        width: Int,
        height: Int,
        count: Int,
        style: String?,
        format: ImageFormat
    ): Result<List<GeneratedImage>> =
        imageGenerationRepository.generateImages(prompt, width, height, count, style, format)
}

class Generate3dModelUseCase @Inject constructor(
    private val model3dGenerationRepository: Model3dGenerationRepository
) {
    suspend operator fun invoke(
        prompt: String,
        format: Model3dFormat,
        detailLevel: DetailLevel,
        targetPolyCount: Int,
        generateTextures: Boolean,
        generateAnimation: Boolean
    ): Result<Generated3dModel> = model3dGenerationRepository.generateModel(
        prompt, format, detailLevel, targetPolyCount, generateTextures, generateAnimation
    )
}

class GenerateGameUseCase @Inject constructor(
    private val gameGenerationRepository: GameGenerationRepository
) {
    suspend operator fun invoke(prompt: String, genre: GameGenre): Result<GeneratedGame> =
        gameGenerationRepository.generateGame(prompt, genre)
}

class ObserveAssetsUseCase @Inject constructor(
    private val assetRepository: AssetRepository
) {
    operator fun invoke(): Flow<List<com.ai3dstudio.mobile.core.domain.model.LibraryAsset>> =
        assetRepository.observeAssets()
}

class SearchAssetsUseCase @Inject constructor(
    private val assetRepository: AssetRepository
) {
    operator fun invoke(query: String): Flow<List<com.ai3dstudio.mobile.core.domain.model.LibraryAsset>> =
        assetRepository.searchAssets(query)
}
