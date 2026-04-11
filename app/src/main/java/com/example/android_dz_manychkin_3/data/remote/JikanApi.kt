package com.example.android_dz_manychkin_3.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApi {
    @GET("top/anime")
    suspend fun getTopAnime(
        @Query("page") page: Int = 1,
    ): JikanListResponse

    @GET("top/manga")
    suspend fun getTopManga(
        @Query("page") page: Int = 1,
    ): JikanListResponse

    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
    ): JikanListResponse

    @GET("manga")
    suspend fun searchManga(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
    ): JikanListResponse

    @GET("anime/{id}")
    suspend fun getAnimeDetail(
        @Path("id") id: Int,
    ): JikanDetailResponse

    @GET("manga/{id}")
    suspend fun getMangaDetail(
        @Path("id") id: Int,
    ): JikanDetailResponse
}