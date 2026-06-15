package com.example.android_dz_manychkin_3.data

import com.example.android_dz_manychkin_3.data.local.FavouriteDao
import com.example.android_dz_manychkin_3.data.local.FavouriteEntity
import com.example.android_dz_manychkin_3.data.remote.JikanApi
import com.example.android_dz_manychkin_3.model.MediaListItem
import com.example.android_dz_manychkin_3.model.MediaType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class JikanRepositoryTest {
    
    private lateinit var api: JikanApi
    private lateinit var dao: FavouriteDao
    private lateinit var repository: JikanRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        api = mockk()
        dao = mockk(relaxed = true)
        repository = JikanRepository(api, dao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observeFavourites should return full item data from Room`() = runTest {
        val favouriteEntity = FavouriteEntity(
            key = "anime-1",
            mediaType = "anime",
            mediaId = 1,
            title = "Test Anime",
            subtitle = "TV",
            score = "8.5",
            format = "TV",
            year = "2024",
            status = "Finished",
            length = "24 min",
            synopsis = "Test"
        )
        
        coEvery { dao.observeByType("anime") } returns flowOf(listOf(favouriteEntity))
        
        val result = mutableListOf<MediaListItem>()
        repository.observeFavourites(MediaType.ANIME).collect { items ->
            result.addAll(items)
        }
        
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Test Anime")
        assertThat(result[0].subtitle).isEqualTo("TV")
        assertThat(result[0].score).isEqualTo("8.5")
    }

    @Test
    fun `setFavourite should save all detail fields to Room`() = runTest {
        val mockResponse = mockk<okhttp3.Response>(relaxed = true)
        
        val detail = createTestMediaDetail()
        
        repository.setFavourite(detail, true)
        
        coVerify {
            dao.upsert(match { entity ->
                entity.mediaId == 1 &&
                entity.title == "Test Anime" &&
                entity.subtitle == "TV" &&
                entity.score == "8.5" &&
                entity.format == "TV" &&
                entity.year == "2024"
            })
        }
    }

    @Test
    fun `setFavourite false should delete from Room`() = runTest {
        val detail = createTestMediaDetail()
        
        repository.setFavourite(detail, false)
        
        coVerify {
            dao.deleteByKey("anime-1")
        }
    }

    @Test
    fun `observeIsFavourite should return existence status`() = runTest {
        coEvery { dao.observeIsFavourite("anime-1") } returns flowOf(1)
        coEvery { dao.observeIsFavourite("anime-99") } returns flowOf(0)
        
        val isFav1 = mutableListOf<Boolean>()
        repository.observeIsFavourite(MediaType.ANIME, 1).collect { isFav1.add(it) }
        
        val isFav99 = mutableListOf<Boolean>()
        repository.observeIsFavourite(MediaType.ANIME, 99).collect { isFav99.add(it) }
        
        assertThat(isFav1).containsExactly(true)
        assertThat(isFav99).containsExactly(false)
    }

    private fun createTestMediaDetail() = com.example.android_dz_manychkin_3.model.MediaDetail(
        id = 1,
        mediaType = MediaType.ANIME,
        title = "Test Anime",
        format = "TV",
        year = "2024",
        score = "8.5",
        status = "Finished Airing",
        length = "24 min per ep",
        synopsis = "Test synopsis"
    )
}
