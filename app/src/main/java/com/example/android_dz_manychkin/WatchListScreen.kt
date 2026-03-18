package com.example.android_dz_manychkin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android_dz_manychkin.model.Movie
import com.example.android_dz_manychkin.model.MovieStatus
import com.example.android_dz_manychkin.model.UiState
@Composable
fun WatchlistScreen(
    uiState: UiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (MovieStatus?) -> Unit,
    onNextStatus: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Мои фильмы",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Поиск фильмов...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { onFilterChange(null) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.selectedFilter == null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                Text("Все")
            }

            Button(
                onClick = { onFilterChange(MovieStatus.PLANNED) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.selectedFilter == MovieStatus.PLANNED)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                Text("В планах")
            }

            Button(
                onClick = { onFilterChange(MovieStatus.WATCHING) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.selectedFilter == MovieStatus.WATCHING)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                Text("Смотрю")
            }

            Button(
                onClick = { onFilterChange(MovieStatus.DONE) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.selectedFilter == MovieStatus.DONE)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                Text("Готово")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Всего: ${uiState.summary.total}")
                Text("В планах: ${uiState.summary.planned}")
                Text("Смотрю: ${uiState.summary.watching}")
                Text("Просмотрено: ${uiState.summary.done}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.filteredMovies) { movie ->
                MovieCard(movie, onNextStatus)
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    onNextStatus: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${movie.year} • ${movie.genre}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = when (movie.status) {
                        MovieStatus.PLANNED -> "В планах"
                        MovieStatus.WATCHING -> "Смотрю"
                        MovieStatus.DONE -> "Просмотрено"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (movie.status) {
                        MovieStatus.PLANNED -> Color(0xFF9C27B0)
                        MovieStatus.WATCHING -> Color(0xFF2196F3)
                        MovieStatus.DONE -> Color(0xFF4CAF50)
                    }
                )
            }

            Button(
                onClick = { onNextStatus(movie.id) }
            ) {
                Text("Далее →")
            }
        }
    }
}