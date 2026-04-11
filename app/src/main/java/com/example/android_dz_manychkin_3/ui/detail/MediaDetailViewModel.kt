package com.example.android_dz_manychkin_3.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_dz_manychkin_3.data.JikanRepository
import com.example.android_dz_manychkin_3.model.MediaType
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class MediaDetailViewModel(
    private val repository: JikanRepository = JikanRepository(),
) : ViewModel() {

    var uiState by mutableStateOf(MediaDetailUiState())
        private set

    private var loadJob: Job? = null

    fun load(mediaType: MediaType, id: Int) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            try {
                val detail = repository.loadMediaDetail(mediaType, id)
                uiState = uiState.copy(
                    detail = detail,
                    isLoading = false,
                    errorMessage = null,
                )
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки",
                )
            }
        }
    }
}