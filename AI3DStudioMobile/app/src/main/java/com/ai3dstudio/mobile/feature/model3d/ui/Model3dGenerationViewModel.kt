package com.ai3dstudio.mobile.feature.model3d.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai3dstudio.mobile.core.domain.model.DetailLevel
import com.ai3dstudio.mobile.core.domain.model.Generated3dModel
import com.ai3dstudio.mobile.core.domain.model.Model3dFormat
import com.ai3dstudio.mobile.core.domain.usecase.Generate3dModelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Model3dUiState(
    val prompt: String = "",
    val format: Model3dFormat = Model3dFormat.GLB,
    val detailLevel: DetailLevel = DetailLevel.MEDIUM,
    val targetPolyCount: Int = 20000,
    val generateTextures: Boolean = true,
    val generateAnimation: Boolean = false,
    val isGenerating: Boolean = false,
    val result: Generated3dModel? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class Model3dGenerationViewModel @Inject constructor(
    private val generate3dModelUseCase: Generate3dModelUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(Model3dUiState())
    val uiState: StateFlow<Model3dUiState> = _uiState.asStateFlow()

    fun onPromptChanged(value: String) { _uiState.value = _uiState.value.copy(prompt = value) }
    fun onFormatChanged(format: Model3dFormat) { _uiState.value = _uiState.value.copy(format = format) }
    fun onDetailLevelChanged(level: DetailLevel) { _uiState.value = _uiState.value.copy(detailLevel = level) }
    fun onPolyCountChanged(count: Int) { _uiState.value = _uiState.value.copy(targetPolyCount = count) }
    fun onTexturesToggled(enabled: Boolean) { _uiState.value = _uiState.value.copy(generateTextures = enabled) }
    fun onAnimationToggled(enabled: Boolean) { _uiState.value = _uiState.value.copy(generateAnimation = enabled) }

    fun generate() {
        val state = _uiState.value
        _uiState.value = state.copy(isGenerating = true, errorMessage = null)
        viewModelScope.launch {
            val result = generate3dModelUseCase(
                prompt = state.prompt,
                format = state.format,
                detailLevel = state.detailLevel,
                targetPolyCount = state.targetPolyCount,
                generateTextures = state.generateTextures,
                generateAnimation = state.generateAnimation
            )
            result.onSuccess { model ->
                _uiState.value = _uiState.value.copy(isGenerating = false, result = model)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isGenerating = false, errorMessage = error.message)
            }
        }
    }
}
