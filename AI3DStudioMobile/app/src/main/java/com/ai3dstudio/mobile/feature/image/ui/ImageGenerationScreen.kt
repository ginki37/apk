package com.ai3dstudio.mobile.feature.image.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ai3dstudio.mobile.R

@Composable
fun ImageGenerationScreen(viewModel: ImageGenerationViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "توليد الصور بالذكاء الاصطناعي", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = uiState.prompt,
            onValueChange = viewModel::onPromptChanged,
            label = { Text(stringResource(R.string.image_prompt_hint)) },
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "${stringResource(R.string.image_count)}: ${uiState.count}")
        Slider(
            value = uiState.count.toFloat(),
            onValueChange = { viewModel.onCountChanged(it.toInt().coerceIn(1, 8)) },
            valueRange = 1f..8f,
            steps = 6
        )

        Button(onClick = viewModel::generate, enabled = !uiState.isGenerating && uiState.prompt.isNotBlank()) {
            if (uiState.isGenerating) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text(stringResource(R.string.image_generate))
        }

        uiState.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
            items(uiState.results, key = { it.id }) { image ->
                AsyncImage(
                    model = image.localPath,
                    contentDescription = image.prompt,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

@Composable
private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id = id)
