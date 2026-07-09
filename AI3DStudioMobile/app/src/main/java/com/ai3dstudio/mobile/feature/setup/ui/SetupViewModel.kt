package com.ai3dstudio.mobile.feature.setup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai3dstudio.mobile.core.domain.model.ServerCapabilities
import com.ai3dstudio.mobile.core.domain.usecase.ConnectToServerUseCase
import com.ai3dstudio.mobile.core.security.RequestValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetupUiState(
    val baseUrlInput: String = "",
    val isConnecting: Boolean = false,
    val connectedCapabilities: ServerCapabilities? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val connectToServerUseCase: ConnectToServerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun onBaseUrlChanged(value: String) {
        _uiState.value = _uiState.value.copy(baseUrlInput = value, errorMessage = null)
    }

    fun connect() {
        val url = _uiState.value.baseUrlInput.trim()
        if (!RequestValidator.isValidBaseUrl(url)) {
            _uiState.value = _uiState.value.copy(errorMessage = "الرجاء إدخال عنوان صحيح يبدأ بـ https://")
            return
        }
        _uiState.value = _uiState.value.copy(isConnecting = true, errorMessage = null)
        viewModelScope.launch {
            val result = connectToServerUseCase(url)
            result.onSuccess { capabilities ->
                _uiState.value = _uiState.value.copy(isConnecting = false, connectedCapabilities = capabilities)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    errorMessage = error.message ?: "تعذر الاتصال بالخادم، تحقق من العنوان والاتصال بالإنترنت"
                )
            }
        }
    }
}
