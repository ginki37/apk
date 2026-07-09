package com.ai3dstudio.mobile.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai3dstudio.mobile.core.domain.model.ServerCapabilities
import com.ai3dstudio.mobile.core.domain.repository.ServerRepository
import com.ai3dstudio.mobile.core.util.FileStorageManager
import com.ai3dstudio.mobile.ui.theme.AppThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val server: ServerCapabilities? = null,
    val cacheSizeBytes: Long = 0L
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val fileStorageManager: FileStorageManager
) : ViewModel() {

    private val themeMode = MutableStateFlow(AppThemeMode.SYSTEM)

    val uiState: StateFlow<SettingsUiState> = combine(
        themeMode, serverRepository.observeActiveServer()
    ) { theme, server ->
        SettingsUiState(themeMode = theme, server = server, cacheSizeBytes = fileStorageManager.totalCacheSizeBytes())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun onThemeModeChanged(mode: AppThemeMode) { themeMode.value = mode }

    fun clearServerAndSignOut() {
        viewModelScope.launch { serverRepository.clearServer() }
    }

    fun clearCache() {
        fileStorageManager.clearCache()
    }
}
