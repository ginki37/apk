package com.ai3dstudio.mobile.feature.setup.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ai3dstudio.mobile.R
import com.ai3dstudio.mobile.core.domain.model.ServerCapabilities

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onConnected: (ServerCapabilities) -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.connectedCapabilities) {
        uiState.connectedCapabilities?.let(onConnected)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringRes(R.string.setup_title), style = MaterialTheme.typography.headlineLarge)
        Text(
            text = stringRes(R.string.setup_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        OutlinedTextField(
            value = uiState.baseUrlInput,
            onValueChange = viewModel::onBaseUrlChanged,
            label = { Text(stringRes(R.string.setup_base_url_label)) },
            placeholder = { Text(stringRes(R.string.setup_base_url_hint)) },
            singleLine = true,
            isError = uiState.errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = viewModel::connect,
            enabled = !uiState.isConnecting && uiState.baseUrlInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            if (uiState.isConnecting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(stringRes(R.string.setup_connect))
            }
        }

        AnimatedVisibility(visible = uiState.isConnecting) {
            Text(
                text = stringRes(R.string.setup_discovering),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun stringRes(id: Int): String = androidx.compose.ui.res.stringResource(id = id)
