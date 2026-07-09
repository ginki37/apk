package com.ai3dstudio.mobile.feature.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ai3dstudio.mobile.R
import com.ai3dstudio.mobile.core.domain.model.GameGenre

@Composable
fun GameGenerationScreen(viewModel: GameGenerationViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "توليد الألعاب بالذكاء الاصطناعي", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = uiState.prompt,
            onValueChange = viewModel::onPromptChanged,
            label = { Text(stringResource(R.string.game_prompt_hint)) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GameGenre.entries.forEach { genre ->
                FilterChip(
                    selected = uiState.genre == genre,
                    onClick = { viewModel.onGenreChanged(genre) },
                    label = { Text(genre.name) }
                )
            }
        }

        Button(onClick = viewModel::generate, enabled = !uiState.isGenerating && uiState.prompt.isNotBlank()) {
            if (uiState.isGenerating) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text(stringResource(R.string.game_generate))
        }

        uiState.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        uiState.result?.let { game ->
            Text(text = "${stringResource(R.string.game_export)}: ${game.zipLocalPath}")
        }
    }
}

@Composable
private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id = id)
