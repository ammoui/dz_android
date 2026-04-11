package com.example.android_dz_manychkin_3.data

import com.example.android_dz_manychkin_3.data.remote.JikanApi
import com.example.android_dz_manychkin_3.data.remote.toDetail
import com.example.android_dz_manychkin_3.data.remote.toListItem
import com.example.android_dz_manychkin_3.model.MediaDetail
import com.example.android_dz_manychkin_3.model.MediaListItem
import com.example.android_dz_manychkin_3.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JikanRepository(
    private val api: JikanApi = NetworkModule.api,
) {
    suspend fun loadMediaList(mediaType: MediaType, query: String): List<MediaListItem> = withContext(Dispatchers.IO) {
        val response = if (query.isBlank()) {
            when (mediaType) {
                MediaType.ANIME -> api.getTopAnime()
                MediaType.MANGA -> api.getTopManga()
            }
        } else {
            when (mediaType) {
                MediaType.ANIME -> api.searchAnime(query = query)
                MediaType.MANGA -> api.searchManga(query = query)
            }
        }

        response.data.mapNotNull { it.toListItem(mediaType) }
    }

    suspend fun loadMediaDetail(mediaType: MediaType, id: Int): MediaDetail = withContext(Dispatchers.IO) {
        val response = when (mediaType) {
            MediaType.ANIME -> api.getAnimeDetail(id)
            MediaType.MANGA -> api.getMangaDetail(id)
        }

        response.data.toDetail(mediaType)
            ?: error("Failed to parse media details")
    }
}