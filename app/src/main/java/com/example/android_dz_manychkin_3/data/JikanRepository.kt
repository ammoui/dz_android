package com.example.android_dz_manychkin_3.data

import com.example.android_dz_manychkin_3.data.local.FavouriteDao
import com.example.android_dz_manychkin_3.data.local.FavouriteEntity
import com.example.android_dz_manychkin_3.data.remote.JikanApi
import com.example.android_dz_manychkin_3.data.remote.toDetail
import com.example.android_dz_manychkin_3.data.remote.toListItem
import com.example.android_dz_manychkin_3.model.MediaDetail
import com.example.android_dz_manychkin_3.model.MediaListItem
import com.example.android_dz_manychkin_3.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JikanRepository @Inject constructor(
    private val api: JikanApi,
    private val favouriteDao: FavouriteDao,
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

    fun observeFavourites(mediaType: MediaType) =
        favouriteDao.observeByType(mediaType.routeValue)
            .map { favourites ->
                favourites.map { entity ->
                    MediaListItem(
                        id = entity.mediaId,
                        mediaType = mediaType,
                        title = entity.title,
                        subtitle = "",
                        score = "—",
                    )
                }
            }

    fun observeIsFavourite(mediaType: MediaType, id: Int): Flow<Boolean> {
        return favouriteDao.observeIsFavourite(favouriteKey(mediaType, id))
            .map { exists -> exists != 0 }
    }

    suspend fun setFavourite(detail: MediaDetail, favourite: Boolean) {
        withContext(Dispatchers.IO) {
            val key = favouriteKey(detail.mediaType, detail.id)

            if (favourite) {
                favouriteDao.upsert(
                    FavouriteEntity(
                        key = key,
                        mediaType = detail.mediaType.routeValue,
                        mediaId = detail.id,
                        title = detail.title,
                    )
                )
            } else {
                favouriteDao.deleteByKey(key)
            }
        }
    }

    private fun favouriteKey(mediaType: MediaType, id: Int): String = "${mediaType.routeValue}-$id"
}