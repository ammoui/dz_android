package com.example.android_dz_manychkin_3.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.android_dz_manychkin_3.data.local.AppDatabase
import com.example.android_dz_manychkin_3.data.local.FavouriteEntity
import com.example.android_dz_manychkin_3.data.remote.ApiListResponse
import com.example.android_dz_manychkin_3.data.remote.JikanApi
import com.example.android_dz_manychkin_3.data.remote.JikanEntry
import com.example.android_dz_manychkin_3.model.MediaDetail
import com.example.android_dz_manychkin_3.model.MediaType
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class RepositoryEndToEndTest {
    
    private lateinit var db: AppDatabase
    private lateinit var dao: com.example.android_dz_manychkin_3.data.local.FavouriteDao
    private lateinit var api: JikanApi
    private lateinit var repository: JikanRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.favouriteDao()
        api = mockk()
        repository = JikanRepository(api, dao, testDispatcher)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun endToEndFlowLoadListAddToFavouritesObserveChanges() = runTest(testDispatcher) {
        val apiResponse = ApiListResponse(
            data = listOf(
                JikanEntry(mal_id = 1, title = "Anime 1", score = 8.5, year = 2024, type = "TV", episodes = null, chapters = null, volumes = null, status = null, synopsis = null),
                JikanEntry(mal_id = 2, title = "Anime 2", score = 8.0, year = 2024, type = "TV", episodes = null, chapters = null, volumes = null, status = null, synopsis = null)
            )
        )
        
        val detail1 = MediaDetail(
            id = 1,
            mediaType = MediaType.ANIME,
            title = "Anime 1",
            format = "TV",
            year = "2024",
            score = "8.5",
            status = "Finished",
            length = "24 min",
            synopsis = "Synopsis 1"
        )
        
        coEvery { api.getTopAnime() } returns apiResponse
        
        // Step 1: Load list
        val loadedList = repository.loadMediaList(MediaType.ANIME, "")
        assertThat(loadedList).hasSize(2)
        
        // Step 2: Add first item to favourites
        repository.setFavourite(detail1, true)
        
        // Step 3: Verify in Room
        val favourites = dao.observeByType("anime").first()
        assertThat(favourites).hasSize(1)
        assertThat(favourites[0].mediaId).isEqualTo(1)
        
        // Step 4: Verify observeIsFavourite returns true
        val isFav = repository.observeIsFavourite(MediaType.ANIME, 1).first()
        assertThat(isFav).isTrue()
        
        // Step 5: Remove from favourites
        repository.setFavourite(detail1, false)
        
        // Step 6: Verify removed
        val emptyFavs = dao.observeByType("anime").first()
        assertThat(emptyFavs).isEmpty()
    }

    @Test
    fun offlineDetailLoadingFallsBackToRoomWhenNetworkFails() = runTest(testDispatcher) {
        val detail = MediaDetail(
            id = 1,
            mediaType = MediaType.ANIME,
            title = "Cached Anime",
            format = "TV",
            year = "2024",
            score = "8.5",
            status = "Finished",
            length = "24 min",
            synopsis = "Synopsis"
        )
        
        // First save to Room
        repository.setFavourite(detail, true)
        
        // Now mock API to throw error
        coEvery { api.getAnimeDetail(any()) } throws java.io.IOException("Network error")
        
        // Should return from Room on fallback
        val result = repository.loadMediaDetailOrFavourite(MediaType.ANIME, 1)
        
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo("Cached Anime")
        assertThat(result?.score).isEqualTo("8.5")
    }

    @Test
    fun shouldMaintainDataConsistencyAcrossRoomAndFlowUpdates() = runTest(testDispatcher) {
        val entity = FavouriteEntity(
            key = "anime-1",
            mediaType = "anime",
            mediaId = 1,
            title = "Anime",
            subtitle = "TV",
            score = "8.5",
            format = "TV",
            year = "2024",
            status = "Finished",
            length = "24 min",
            synopsis = "Synopsis"
        )
        
        dao.upsert(entity)
        
        // Observe initial state
        var flowItems = repository.observeFavourites(MediaType.ANIME).first()
        assertThat(flowItems).hasSize(1)
        assertThat(flowItems[0].score).isEqualTo("8.5")
        
        val detail = MediaDetail(
            id = 1,
            mediaType = MediaType.ANIME,
            title = "Anime",
            format = "TV",
            year = "2024",
            score = "9.0",
            status = "Finished",
            length = "24 min",
            synopsis = "Synopsis"
        )
        
        // Update through Repository
        repository.setFavourite(detail, true)
        
        // Flow should emit updated data
        flowItems = repository.observeFavourites(MediaType.ANIME).first()
        assertThat(flowItems[0].score).isEqualTo("9.0")
    }
}
