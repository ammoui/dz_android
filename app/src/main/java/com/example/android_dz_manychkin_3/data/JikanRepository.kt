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
import kotlinx.coroutines.flow.distinctUntilChanged
import java.io.IOException
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton
import com.example.android_dz_manychkin_3.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher

@Singleton
class JikanRepository @Inject constructor(
    private val api: JikanApi,
    private val favouriteDao: FavouriteDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun loadMediaList(mediaType: MediaType, query: String): List<MediaListItem> = withContext(ioDispatcher) {
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

    suspend fun loadMediaDetail(mediaType: MediaType, id: Int): MediaDetail = withContext(ioDispatcher) {
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
                        subtitle = entity.subtitle,
                        score = entity.score,
                    )
                }
            }

    fun observeIsFavourite(mediaType: MediaType, id: Int): Flow<Boolean> {
        return favouriteDao.observeIsFavourite(favouriteKey(mediaType, id))
            .map { exists -> exists != 0 }
            .distinctUntilChanged()
    }

    suspend fun setFavourite(detail: MediaDetail, favourite: Boolean) {
        withContext(ioDispatcher) {
            val key = favouriteKey(detail.mediaType, detail.id)

            if (favourite) {
                favouriteDao.upsert(
                    FavouriteEntity(
                        key = key,
                        mediaType = detail.mediaType.routeValue,
                        mediaId = detail.id,
                        title = detail.title,
                        subtitle = detail.format,
                        score = detail.score,
                        format = detail.format,
                        year = detail.year,
                        status = detail.status,
                        length = detail.length,
                        synopsis = detail.synopsis,
                    )
                )
            } else {
                favouriteDao.deleteByKey(key)
            }
        }
    }

    suspend fun loadMediaDetailOrFavourite(mediaType: MediaType, id: Int): MediaDetail? = withContext(ioDispatcher) {
        try {
            val response = when (mediaType) {
                MediaType.ANIME -> api.getAnimeDetail(id)
                MediaType.MANGA -> api.getMangaDetail(id)
            }
            response.data.toDetail(mediaType)
        } catch (e: Exception) {
            when (e) {
                is IOException, is HttpException -> {
                    val favouriteKey = favouriteKey(mediaType, id)
                    favouriteDao.getByKey(favouriteKey)?.let { entity ->
                        MediaDetail(
                            id = entity.mediaId,
                            mediaType = mediaType,
                            title = entity.title,
                            format = entity.format,
                            year = entity.year,
                            score = entity.score,
                            status = entity.status,
                            length = entity.length,
                            synopsis = entity.synopsis,
                        )
                    }
                }
                else -> throw e
            }
        }
    }

    private fun favouriteKey(mediaType: MediaType, id: Int): String = "${mediaType.routeValue}-$id"
}