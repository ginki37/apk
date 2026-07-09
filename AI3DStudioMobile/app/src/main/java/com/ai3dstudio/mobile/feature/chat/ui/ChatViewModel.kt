package com.ai3dstudio.mobile.feature.chat.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai3dstudio.mobile.core.domain.model.ChatMessage
import com.ai3dstudio.mobile.core.domain.usecase.ObserveChatMessagesUseCase
import com.ai3dstudio.mobile.core.domain.usecase.SendChatMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val inputText: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeChatMessagesUseCase: ObserveChatMessagesUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase
) : ViewModel() {

    val projectId: String = checkNotNull(savedStateHandle["projectId"])

    val messages: StateFlow<List<ChatMessage>> = observeChatMessagesUseCase(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(inputText = value)
    }

    fun send() {
        val text = _uiState.value.inputText
        if (text.isBlank()) return
        _uiState.value = _uiState.value.copy(isSending = true, inputText = "", errorMessage = null)
        viewModelScope.launch {
            val result = sendChatMessageUseCase(projectId, text)
            _uiState.value = _uiState.value.copy(
                isSending = false,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }
}
