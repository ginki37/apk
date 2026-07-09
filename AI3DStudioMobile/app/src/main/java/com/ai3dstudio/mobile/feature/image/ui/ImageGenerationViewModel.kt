package com.ai3dstudio.mobile.feature.image.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai3dstudio.mobile.core.domain.model.GeneratedImage
import com.ai3dstudio.mobile.core.domain.model.ImageFormat
import com.ai3dstudio.mobile.core.domain.usecase.GenerateImagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImageGenUiState(
    val prompt: String = "",
    val width: Int = 1024,
    val height: Int = 1024,
    val count: Int = 1,
    val style: String = "photorealistic",
    val format: ImageFormat = ImageFormat.PNG,
    val isGenerating: Boolean = false,
    val results: List<GeneratedImage> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ImageGenerationViewModel @Inject constructor(
    private val generateImagesUseCase: GenerateImagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageGenUiState())
    val uiState: StateFlow<ImageGenUiState> = _uiState.asStateFlow()

    fun onPromptChanged(value: String) { _uiState.value = _uiState.value.copy(prompt = value) }
    fun onSizeChanged(width: Int, height: Int) { _uiState.value = _uiState.value.copy(width = width, height = height) }
    fun onCountChanged(count: Int) { _uiState.value = _uiState.value.copy(count = count) }
    fun onStyleChanged(style: String) { _uiState.value = _uiState.value.copy(style = style) }
    fun onFormatChanged(format: ImageFormat) { _uiState.value = _uiState.value.copy(format = format) }

    fun generate() {
        val state = _uiState.value
        _uiState.value = state.copy(isGenerating = true, errorMessage = null)
        viewModelScope.launch {
            val result = generateImagesUseCase(
                prompt = state.prompt,
                width = state.width,
                height = state.height,
                count = state.count,
                style = state.style,
                format = state.format
            )
            result.onSuccess { images ->
                _uiState.value = _uiState.value.copy(isGenerating = false, results = images)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isGenerating = false, errorMessage = error.message)
            }
        }
    }
}
