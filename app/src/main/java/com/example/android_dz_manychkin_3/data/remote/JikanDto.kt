package com.example.android_dz_manychkin_3.data.remote

import com.example.android_dz_manychkin_3.model.MediaDetail
import com.example.android_dz_manychkin_3.model.MediaListItem
import com.example.android_dz_manychkin_3.model.MediaType
import com.google.gson.annotations.SerializedName
import java.util.Locale

data class JikanListResponse(
    val data: List<JikanMediaDto> = emptyList(),
)

data class JikanDetailResponse(
    val data: JikanMediaDto = JikanMediaDto(),
)

data class JikanMediaDto(
    @SerializedName("mal_id")
    val id: Int = 0,
    val url: String? = null,
    val title: String? = null,
    val score: Double? = null,
    val year: Int? = null,
    val episodes: Int? = null,
    val chapters: Int? = null,
    val volumes: Int? = null,
    val synopsis: String? = null,
    val status: String? = null,
    val type: String? = null,
    val genres: List<JikanNamedDto>? = null,
    val authors: List<JikanNamedDto>? = null,
    val studios: List<JikanNamedDto>? = null,
    val images: JikanImagesDto? = null,
    val aired: JikanPeriodDto? = null,
    val published: JikanPeriodDto? = null,
)

data class JikanNamedDto(
    val name: String? = null,
)

data class JikanImagesDto(
    val jpg: JikanImageDto? = null,
)

data class JikanImageDto(
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("large_image_url")
    val largeImageUrl: String? = null,
)

data class JikanPeriodDto(
    val prop: JikanPeriodPropDto? = null,
    val string: String? = null,
)

data class JikanPeriodPropDto(
    val from: JikanDateDto? = null,
)

data class JikanDateDto(
    val year: Int? = null,
)

fun JikanMediaDto.toListItem(mediaType: MediaType): MediaListItem? {
    val safeTitle = title ?: return null
    val scoreText = score?.let { String.format(Locale.US, "%.1f", it) } ?: "-"
    val yearText = year ?: when (mediaType) {
        MediaType.ANIME -> aired?.prop?.from?.year
        MediaType.MANGA -> published?.prop?.from?.year
    }

    val lengthText = when (mediaType) {
        MediaType.ANIME -> episodes?.let { "$it episodes" }
        MediaType.MANGA -> listOfNotNull(chapters?.let { "$it chapters" }, volumes?.let { "$it volumes" })
            .joinToString(", ")
            .ifBlank { null }
    }

    val subtitle = listOfNotNull(
        yearText?.toString(),
        lengthText,
        type,
    ).joinToString(" • ")

    return MediaListItem(
        id = id,
        mediaType = mediaType,
        title = safeTitle,
        subtitle = subtitle,
        score = scoreText,
    )
}

fun JikanMediaDto.toDetail(mediaType: MediaType): MediaDetail? {
    val safeTitle = title ?: return null
    val scoreText = score?.let { String.format(Locale.US, "%.1f", it) } ?: "-"
    val yearText = year ?: when (mediaType) {
        MediaType.ANIME -> aired?.prop?.from?.year
        MediaType.MANGA -> published?.prop?.from?.year
    }

    val formatText = type ?: mediaType.label
    val lengthText = when (mediaType) {
        MediaType.ANIME -> episodes?.let { "$it episodes" }
        MediaType.MANGA -> listOfNotNull(chapters?.let { "$it chapters" }, volumes?.let { "$it volumes" })
            .joinToString(", ")
            .ifBlank { null }
    } ?: "-"

    val genresText = genres
        ?.mapNotNull { it.name }
        .orEmpty()

    return MediaDetail(
        id = id,
        mediaType = mediaType,
        title = safeTitle,
        format = formatText,
        score = scoreText,
        status = status ?: "-",
        length = lengthText,
        year = yearText?.toString() ?: "-",
        synopsis = synopsis?.trim().orEmpty().ifBlank { "No description available." },
        genres = genresText,
        sourceUrl = url,
    )
}