package com.example.android_dz_manychkin_3.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.android_dz_manychkin_3.model.MediaListItem
import com.example.android_dz_manychkin_3.model.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaListScreen(
    uiState: MediaListUiState,
    onEvent: (MediaListEvent) -> Unit,
    onOpenDetail: (MediaType, Int) -> Unit,
) {
    val focusRequester = FocusRequester()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Jikan Explorer") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { onEvent(MediaListEvent.QueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text("Search anime or manga") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )

            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = uiState.mediaType == MediaType.ANIME,
                    onClick = { onEvent(MediaListEvent.MediaTypeChanged(MediaType.ANIME)) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) {
                    Text("Anime")
                }
                SegmentedButton(
                    selected = uiState.mediaType == MediaType.MANGA,
                    onClick = { onEvent(MediaListEvent.MediaTypeChanged(MediaType.MANGA)) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) {
                    Text("Manga")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val hasResults = uiState.items.isNotEmpty() ||
                (uiState.query.isBlank() && uiState.favouriteItems.isNotEmpty())

            val showFullScreenLoading = uiState.isLoading && uiState.items.isEmpty()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when {
                    uiState.errorMessage != null -> {
                        ListErrorState(
                            message = uiState.errorMessage,
                            onRetry = { onEvent(MediaListEvent.Retry) },
                        )
                    }

                    showFullScreenLoading -> {
                        ListLoadingState()
                    }

                    !hasResults -> {
                        val title = if (uiState.query.isBlank()) {
                            "Нет избранного"
                        } else {
                            "Ничего не найдено"
                        }
                        val subtitle = if (uiState.query.isBlank()) {
                            "Добавьте тайтлы в избранное, чтобы увидеть их здесь."
                        } else {
                            "Попробуйте другой запрос или переключите тип ресурса."
                        }
                        EmptyState(
                            title = title,
                            subtitle = subtitle,
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                        ) {
                            if (uiState.isLoading) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }

                            if (uiState.query.isBlank() && uiState.favouriteItems.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Избранное",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }

                                items(
                                    items = uiState.favouriteItems,
                                    key = { "fav-${it.id}" },
                                ) { item ->
                                    MediaCard(
                                        item = item,
                                        isFavourite = true,
                                        onClick = { onOpenDetail(item.mediaType, item.id) },
                                    )
                                }
                            }

                            if (uiState.query.isBlank() && uiState.items.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Популярное",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }

                            items(
                                items = uiState.items,
                                key = { it.id },
                            ) { item ->
                                MediaCard(
                                    item = item,
                                    isFavourite = uiState.favouriteIds.contains(item.id),
                                    onClick = { onOpenDetail(item.mediaType, item.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListLoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text("Loading...")
    }
}

@Composable
private fun ListErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun MediaCard(
    item: MediaListItem,
    isFavourite: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.subtitle.ifBlank { item.mediaType.label },
            style = MaterialTheme.typography.bodyMedium,
        )
        if (isFavourite) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "★ Избранное",
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Score: ${item.score}",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}