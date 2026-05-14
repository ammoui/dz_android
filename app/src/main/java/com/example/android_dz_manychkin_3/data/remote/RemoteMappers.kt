package com.example.android_dz_manychkin_3.data.remote

import com.example.android_dz_manychkin_3.model.MediaDetail
import com.example.android_dz_manychkin_3.model.MediaListItem
import com.example.android_dz_manychkin_3.model.MediaType

fun JikanEntry.toListItem(mediaType: MediaType): MediaListItem? {
    val safeTitle = title ?: return null
    val subtitle = type ?: (year?.toString() ?: mediaType.label)
    val scoreText = score?.toString() ?: "—"
    return MediaListItem(
        id = mal_id,
        mediaType = mediaType,
        title = safeTitle,
        subtitle = subtitle,
        score = scoreText,
    )
}

fun JikanEntry.toDetail(mediaType: MediaType): MediaDetail? {
    val safeTitle = title ?: return null
    val formatText = type ?: mediaType.label
    val yearText = year?.toString() ?: "—"
    val scoreText = score?.toString() ?: "—"
    val statusText = status ?: "Unknown"
    val lengthText = when (mediaType) {
        MediaType.ANIME -> episodes?.let { "$it eps" } ?: "—"
        MediaType.MANGA -> when {
            chapters != null -> "$chapters ch"
            volumes != null -> "$volumes vol"
            else -> "—"
        }
    }
    val synopsisText = synopsis ?: "No description."

    return MediaDetail(
        id = mal_id,
        mediaType = mediaType,
        title = safeTitle,
        format = formatText,
        year = yearText,
        score = scoreText,
        status = statusText,
        length = lengthText,
        synopsis = synopsisText,
    )
}
