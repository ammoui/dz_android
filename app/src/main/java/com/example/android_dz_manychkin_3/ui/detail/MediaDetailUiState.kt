package com.example.android_dz_manychkin_3.ui.detail

import com.example.android_dz_manychkin_3.model.MediaDetail

data class MediaDetailUiState(
    val detail: MediaDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)