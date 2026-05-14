package com.example.android_dz_manychkin_3.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_dz_manychkin_3.data.JikanRepository
import com.example.android_dz_manychkin_3.model.MediaDetail
import com.example.android_dz_manychkin_3.model.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private const val DETAIL_MEDIA_TYPE_ARGUMENT = "mediaType"
private const val DETAIL_ID_ARGUMENT = "id"

@HiltViewModel
class MediaDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: JikanRepository,
) : ViewModel() {

    var uiState by mutableStateOf<MediaDetailUiState>(MediaDetailUiState.Loading)
        private set

    private var loadJob: Job? = null
    private var favouriteJob: Job? = null
    private var mediaType: MediaType? = null
    private var mediaId: Int? = null
    private var currentDetail: MediaDetail? = null

    init {
        val typeValue = savedStateHandle.get<String>(DETAIL_MEDIA_TYPE_ARGUMENT)
        val idValue = savedStateHandle.get<Int>(DETAIL_ID_ARGUMENT)

        val parsedType = MediaType.fromRouteValueOrNull(typeValue)

        if (parsedType == null || idValue == null) {
            uiState = MediaDetailUiState.Error("Ошибка аргументов навигации")
        } else {
            mediaType = parsedType
            mediaId = idValue
            load()
        }
    }

    fun retry() {
        load()
    }

    fun toggleFavourite() {
        val detail = currentDetail ?: return
        val isFavourite = (uiState as? MediaDetailUiState.Content)?.isFavourite ?: false

        viewModelScope.launch {
            repository.setFavourite(detail, !isFavourite)
        }
    }

    private fun load() {
        val type = mediaType ?: return
        val id = mediaId ?: return
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            uiState = MediaDetailUiState.Loading

            try {
                val detail = repository.loadMediaDetail(type, id)
                currentDetail = detail
                uiState = MediaDetailUiState.Content(detail, isFavourite = false)
                startObserveFavourite(detail)
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (_: Exception) {
                uiState = MediaDetailUiState.Error("Ошибка загрузки")
            }
        }
    }

    private fun startObserveFavourite(detail: MediaDetail) {
        favouriteJob?.cancel()
        favouriteJob = viewModelScope.launch {
            repository.observeIsFavourite(detail.mediaType, detail.id).collect { isFavourite ->
                val current = uiState
                if (current is MediaDetailUiState.Content && current.detail.id == detail.id) {
                    uiState = current.copy(isFavourite = isFavourite)
                }
            }
        }
    }
}