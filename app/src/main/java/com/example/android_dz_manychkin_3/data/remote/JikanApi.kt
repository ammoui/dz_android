package com.example.android_dz_manychkin_3.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApi {
    @GET("top/anime")
    suspend fun getTopAnime(): ApiListResponse<JikanEntry>

    @GET("top/manga")
    suspend fun getTopManga(): ApiListResponse<JikanEntry>

    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
    ): ApiListResponse<JikanEntry>

    @GET("manga")
    suspend fun searchManga(
        @Query("q") query: String,
    ): ApiListResponse<JikanEntry>

    @GET("anime/{id}")
    suspend fun getAnimeDetail(
        @Path("id") id: Int,
    ): ApiSingleResponse<JikanEntry>

    @GET("manga/{id}")
    suspend fun getMangaDetail(
        @Path("id") id: Int,
    ): ApiSingleResponse<JikanEntry>
}
