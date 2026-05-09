package com.example.android_dz_manychkin_3.model

data class MediaListItem(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val subtitle: String,
    val score: String,
)

data class MediaDetail(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val format: String,
    val score: String,
    val status: String,
    val length: String,
    val year: String,
    val synopsis: String,
    val genres: List<String>,
    val sourceUrl: String?,
)