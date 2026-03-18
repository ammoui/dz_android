package com.example.android_dz_manychkin.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.android_dz_manychkin.model.Movie
import com.example.android_dz_manychkin.model.MovieStatus
import com.example.android_dz_manychkin.model.Summary
import com.example.android_dz_manychkin.model.UiState

class WatchlistStateHolder {
    private val initialFilms = listOf(
        Movie(1, "Начало", 2010, "Фантастика", MovieStatus.PLANNED),
        Movie(2, "Зеленая миля", 1999, "Драма", MovieStatus.WATCHING),
        Movie(3, "Форрест Гамп", 1994, "Драма", MovieStatus.DONE),
        Movie(4, "Матрица", 1999, "Фантастика", MovieStatus.PLANNED),
        Movie(5, "Интерстеллар", 2014, "Фантастика", MovieStatus.WATCHING),
        Movie(6, "Криминальное чтиво", 1994, "Криминал", MovieStatus.PLANNED),
        Movie(7, "Бойцовский клуб", 1999, "Триллер", MovieStatus.DONE),
        Movie(8, "Побег из Шоушенка", 1994, "Драма", MovieStatus.DONE),
        Movie(9, "Темный рыцарь", 2008, "Экшн", MovieStatus.PLANNED),
        Movie(10, "1+1", 2011, "Драма", MovieStatus.WATCHING)
    )


    private val totalCount = initialFilms.size
    private val plannedCount = initialFilms.count { it.status == MovieStatus.PLANNED }
    private val watchingCount = initialFilms.count { it.status == MovieStatus.WATCHING }
    private val doneCount = initialFilms.count { it.status == MovieStatus.DONE }


    var uiState by mutableStateOf(
        UiState(
            allMovies = initialFilms,
            searchQuery = "",
            selectedFilter = null,
            filteredMovies = initialFilms,
            summary = Summary(
                total = totalCount,
                planned = plannedCount,
                watching = watchingCount,
                done = doneCount
            )
        )
    )
        private set


    fun onSearchQueryChange(query: String) {

        val result = uiState.allMovies.filter { movie ->
            movie.title.contains(query, ignoreCase = true)
        }


        uiState = uiState.copy(
            searchQuery = query,
            filteredMovies = result,
            summary = Summary(
                total = result.size,
                planned = result.count { it.status == MovieStatus.PLANNED },
                watching = result.count { it.status == MovieStatus.WATCHING },
                done = result.count { it.status == MovieStatus.DONE }
            )
        )
    }


    fun onFilterChange(filter: MovieStatus?) {

        val afterSearch = uiState.allMovies.filter { movie ->
            uiState.searchQuery.isEmpty() ||
                    movie.title.contains(uiState.searchQuery, ignoreCase = true)
        }


        val result = if (filter == null) {
            afterSearch
        } else {
            afterSearch.filter { it.status == filter }
        }


        uiState = uiState.copy(
            selectedFilter = filter,
            filteredMovies = result,
            summary = Summary(
                total = result.size,
                planned = result.count { it.status == MovieStatus.PLANNED },
                watching = result.count { it.status == MovieStatus.WATCHING },
                done = result.count { it.status == MovieStatus.DONE }
            )
        )
    }


    fun onNextStatus(movieId: Int) {
        val updatedList = uiState.allMovies.map { movie ->
            if (movie.id == movieId) {

                val newStatus = when (movie.status) {
                    MovieStatus.PLANNED -> MovieStatus.WATCHING
                    MovieStatus.WATCHING -> MovieStatus.DONE
                    MovieStatus.DONE -> MovieStatus.PLANNED
                }
                movie.copy(status = newStatus)
            } else {
                movie
            }
        }


        val afterSearch = updatedList.filter { movie ->
            uiState.searchQuery.isEmpty() ||
                    movie.title.contains(uiState.searchQuery, ignoreCase = true)
        }

        val afterFilter = if (uiState.selectedFilter == null) {
            afterSearch
        } else {
            afterSearch.filter { it.status == uiState.selectedFilter }
        }

        uiState = uiState.copy(
            allMovies = updatedList,
            filteredMovies = afterFilter,
            summary = Summary(
                total = afterFilter.size,
                planned = afterFilter.count { it.status == MovieStatus.PLANNED },
                watching = afterFilter.count { it.status == MovieStatus.WATCHING },
                done = afterFilter.count { it.status == MovieStatus.DONE }
            )
        )
    }
}