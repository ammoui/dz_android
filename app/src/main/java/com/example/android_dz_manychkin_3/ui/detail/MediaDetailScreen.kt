package com.example.android_dz_manychkin_3.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.android_dz_manychkin_3.model.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    uiState: MediaDetailUiState,
    mediaType: MediaType,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavourite: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mediaType.label) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (uiState) {
            MediaDetailUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading...")
                }
            }

            is MediaDetailUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }

            is MediaDetailUiState.Content -> {
                val detail = uiState.detail
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        Text(
                            text = detail.title,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${detail.format} • ${detail.year} • ${detail.score}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(onClick = onToggleFavourite) {
                                Text(
                                    text = if (uiState.isFavourite) "Remove from favourites" else "Add to favourites",
                                )
                            }
                    }

                    item {
                        HorizontalDivider()
                    }

                    item {
                        DetailRow(label = "Status", value = detail.status)
                        DetailRow(label = "Length", value = detail.length)
                        DetailRow(label = "Type", value = detail.format)
                        DetailRow(label = "Year", value = detail.year)
                        DetailRow(label = "Score", value = detail.score)
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = detail.synopsis,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}