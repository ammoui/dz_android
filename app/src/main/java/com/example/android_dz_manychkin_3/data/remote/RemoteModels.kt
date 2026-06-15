package com.example.android_dz_manychkin_3.data.remote

data class ApiListResponse<T>(
    val data: List<T>,
)

data class ApiSingleResponse<T>(
    val data: T,
)

data class JikanEntry(
    val mal_id: Int,
    val title: String?,
    val score: Double?,
    val year: Int?,
    val type: String?,
    val episodes: Int?,
    val chapters: Int?,
    val volumes: Int?,
    val status: String?,
    val synopsis: String?,
)
