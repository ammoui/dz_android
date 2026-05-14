package com.example.android_dz_manychkin_3.ui.list

import com.example.android_dz_manychkin_3.model.MediaListItem
import com.example.android_dz_manychkin_3.model.MediaType

data class MediaListUiState(
    val query: String = "",
    val mediaType: MediaType = MediaType.ANIME,
    val items: List<MediaListItem> = emptyList(),
    val favouriteItems: List<MediaListItem> = emptyList(),
    val favouriteIds: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface MediaListEvent {
    data class QueryChanged(val value: String) : MediaListEvent
    data class MediaTypeChanged(val value: MediaType) : MediaListEvent
    data object Retry : MediaListEvent
}