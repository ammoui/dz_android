package com.example.android_dz_manychkin_3.ui.list

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_dz_manychkin_3.data.JikanRepository
import com.example.android_dz_manychkin_3.model.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import retrofit2.HttpException
import java.io.IOException

@HiltViewModel
class MediaListViewModel @Inject constructor(
    private val repository: JikanRepository,
) : ViewModel() {

    var uiState by mutableStateOf(MediaListUiState())
        private set

    private var loadJob: Job? = null
    private var favouritesJob: Job? = null
    private var loadRequestId: Int = 0

    init {
        startObserveFavourites(uiState.mediaType)
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
                startObserveFavourites(event.value)
                loadCurrentList()
            }

            MediaListEvent.Retry -> loadCurrentList()
        }
    }

    private fun loadCurrentList(debounce: Boolean = false) {
        loadJob?.cancel()

        val query = uiState.query.trim()
        val mediaType = uiState.mediaType
        val requestId = ++loadRequestId

        loadJob = viewModelScope.launch {
            if (debounce) {
                delay(350)
            }

            uiState = uiState.copy(isLoading = true, errorMessage = null)

            try {
                val items = withTimeout(20_000) {
                    repository.loadMediaList(mediaType, query)
                }

                if (query != uiState.query.trim() || mediaType != uiState.mediaType) {
                    return@launch
                }

                uiState = uiState.copy(
                    items = items,
                    isLoading = false,
                    errorMessage = null,
                )
            } catch (timeout: TimeoutCancellationException) {
                Log.w("MediaListViewModel", "Load list timeout", timeout)
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Долго отвечает сервер. Проверьте интернет.",
                )
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (http: HttpException) {
                Log.w("MediaListViewModel", "HTTP error ${http.code()}", http)
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Ошибка сервера (${http.code()}).",
                )
            } catch (io: IOException) {
                Log.w("MediaListViewModel", "Network error", io)
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Проблема с интернетом. Проверьте соединение.",
                )
            } catch (exception: Exception) {
                Log.w("MediaListViewModel", "Unknown error", exception)
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки",
                )
            } finally {
                if (requestId == loadRequestId && uiState.isLoading) {
                    uiState = uiState.copy(isLoading = false)
                }
            }
        }
    }

    private fun startObserveFavourites(mediaType: MediaType) {
        favouritesJob?.cancel()
        favouritesJob = viewModelScope.launch {
            repository.observeFavourites(mediaType).collect { favourites ->
                val favouriteIds = favourites.map { it.id }.toSet()
                uiState = uiState.copy(
                    favouriteItems = favourites,
                    favouriteIds = favouriteIds,
                )
            }
        }
    }
}