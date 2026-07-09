package com.ai3dstudio.mobile.feature.model3d.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ai3dstudio.mobile.R
import com.ai3dstudio.mobile.core.domain.model.Model3dFormat

@Composable
fun Model3dGenerationScreen(viewModel: Model3dGenerationViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "توليد نماذج ثلاثية الأبعاد", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = uiState.prompt,
            onValueChange = viewModel::onPromptChanged,
            label = { Text(stringResource(R.string.model3d_prompt_hint)) },
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "${stringResource(R.string.model3d_poly_count)}: ${uiState.targetPolyCount}")
        Slider(
            value = uiState.targetPolyCount.toFloat(),
            onValueChange = { viewModel.onPolyCountChanged(it.toInt()) },
            valueRange = 1000f..200000f
        )

        Button(onClick = viewModel::generate, enabled = !uiState.isGenerating && uiState.prompt.isNotBlank()) {
            if (uiState.isGenerating) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text(stringResource(R.string.model3d_generate))
        }

        uiState.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        uiState.result?.let { model ->
            Text(text = stringResource(R.string.model3d_view), style = MaterialTheme.typography.titleLarge)
            Model3dViewerScreen(modelFilePath = model.localPath)
        }
    }
}

@Composable
private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id = id)
