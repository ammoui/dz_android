package com.example.android_dz_manychkin_3.model

enum class MediaType(val routeValue: String, val label: String) {
    ANIME("anime", "Anime"),
    MANGA("manga", "Manga");

    companion object {
        fun fromRouteValueOrNull(value: String?): MediaType? {
            return entries.firstOrNull { it.routeValue == value }
        }
    }
}