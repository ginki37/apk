package com.ai3dstudio.mobile.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ai3dstudio.mobile.R
import com.ai3dstudio.mobile.ui.theme.AppThemeMode

@Composable
fun SettingsScreen(
    onChangeServer: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.themeMode == AppThemeMode.SYSTEM,
                onClick = { viewModel.onThemeModeChanged(AppThemeMode.SYSTEM) },
                label = { Text(stringResource(R.string.settings_theme_system)) }
            )
            FilterChip(
                selected = uiState.themeMode == AppThemeMode.LIGHT,
                onClick = { viewModel.onThemeModeChanged(AppThemeMode.LIGHT) },
                label = { Text(stringResource(R.string.settings_theme_light)) }
            )
            FilterChip(
                selected = uiState.themeMode == AppThemeMode.DARK,
                onClick = { viewModel.onThemeModeChanged(AppThemeMode.DARK) },
                label = { Text(stringResource(R.string.settings_theme_dark)) }
            )
        }

        Text(text = stringResource(R.string.settings_server), style = MaterialTheme.typography.titleLarge)
        Text(text = uiState.server?.baseUrl ?: "غير متصل")
        OutlinedButton(onClick = {
            viewModel.clearServerAndSignOut()
            onChangeServer()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_change_url))
        }

        Text(text = stringResource(R.string.settings_security), style = MaterialTheme.typography.titleLarge)
        Text(text = "حجم الذاكرة المؤقتة: ${uiState.cacheSizeBytes / 1024} كيلوبايت")
        Button(onClick = viewModel::clearCache, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_clear_cache))
        }
    }
}

@Composable
private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id = id)
