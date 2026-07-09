package com.ai3dstudio.mobile.feature.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai3dstudio.mobile.core.domain.model.GameGenre
import com.ai3dstudio.mobile.core.domain.model.GeneratedGame
import com.ai3dstudio.mobile.core.domain.usecase.GenerateGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameGenUiState(
    val prompt: String = "",
    val genre: GameGenre = GameGenre.SURVIVAL,
    val isGenerating: Boolean = false,
    val result: GeneratedGame? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class GameGenerationViewModel @Inject constructor(
    private val generateGameUseCase: GenerateGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameGenUiState())
    val uiState: StateFlow<GameGenUiState> = _uiState.asStateFlow()

    fun onPromptChanged(value: String) { _uiState.value = _uiState.value.copy(prompt = value) }
    fun onGenreChanged(genre: GameGenre) { _uiState.value = _uiState.value.copy(genre = genre) }

    fun generate() {
        val state = _uiState.value
        _uiState.value = state.copy(isGenerating = true, errorMessage = null)
        viewModelScope.launch {
            val result = generateGameUseCase(state.prompt, state.genre)
            result.onSuccess { game ->
                _uiState.value = _uiState.value.copy(isGenerating = false, result = game)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isGenerating = false, errorMessage = error.message)
            }
        }
    }
}
