package com.example.android_dz_manychkin_3.ui.detail

import com.example.android_dz_manychkin_3.model.MediaDetail

sealed interface MediaDetailUiState {
    data object Loading : MediaDetailUiState
    data class Error(val message: String) : MediaDetailUiState
    data class Content(
        val detail: MediaDetail,
        val isFavourite: Boolean,
    ) : MediaDetailUiState
}