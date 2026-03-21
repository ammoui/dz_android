package com.example.android_dz_manychkin.model

enum class  MovieStatus {
    PLANNED,
    WATCHING,
    DONE
}

data class Movie (
    val id: Int,
    val title: String,
    val year: Int,
    val genre: String,
    val status: MovieStatus
)
