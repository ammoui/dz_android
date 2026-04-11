package com.example.android_dz_manychkin_3.ui.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_dz_manychkin_3.data.JikanRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class MediaListViewModel(
    private val repository: JikanRepository = JikanRepository(),
) : ViewModel() {

    var uiState by mutableStateOf(MediaListUiState())
        private set

    private var loadJob: Job? = null

    init {
        loadCurrentList()
    }

    fun onEvent(event: MediaListEvent) {
        when (event) {
            is MediaListEvent.QueryChanged -> {
                uiState = uiState.copy(query = event.value, errorMessage = null)
                loadCurrentList(debounce = true)
            }

            is MediaListEvent.MediaTypeChanged -> {
                uiState = uiState.copy(mediaType = event.value, errorMessage = null)
                loadCurrentList()
            }

            MediaListEvent.Retry -> loadCurrentList()
        }
    }

    private fun loadCurrentList(debounce: Boolean = false) {
        loadJob?.cancel()

        val query = uiState.query.trim()
        val mediaType = uiState.mediaType

        loadJob = viewModelScope.launch {
            if (debounce) {
                delay(350)
            }

            uiState = uiState.copy(isLoading = true, errorMessage = null)

            try {
                val items = repository.loadMediaList(mediaType, query)

                if (query != uiState.query.trim() || mediaType != uiState.mediaType) {
                    return@launch
                }

                uiState = uiState.copy(
                    items = items,
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