package com.example.android_dz_manychkin.model

data class Summary(
    val total: Int = 0,
    val planned: Int = 0,
    val watching: Int = 0,
    val done: Int = 0
)

data class UiState(
    val allMovies: List<Movie> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: MovieStatus? = null,
    val filteredMovies: List<Movie> = emptyList(),
    val summary: Summary = Summary()
)