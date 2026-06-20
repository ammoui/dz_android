package com.example.android_dz_manychkin_3.data

import com.example.android_dz_manychkin_3.data.local.FavouriteDao
import com.example.android_dz_manychkin_3.data.local.FavouriteEntity
import com.example.android_dz_manychkin_3.data.remote.JikanApi
import com.example.android_dz_manychkin_3.model.MediaType
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import app.cash.turbine.test

@OptIn(ExperimentalCoroutinesApi::class)
class JikanRepositoryFlowSimpleTest {
    
    private lateinit var api: JikanApi
    private lateinit var dao: FavouriteDao
    private lateinit var repository: JikanRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        api = mockk()
        dao = mockk(relaxed = true)
        repository = JikanRepository(api, dao, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observeFavourites returns initial empty list from Flow`() = runTest {
        val favFlow = MutableStateFlow<List<FavouriteEntity>>(emptyList())
        every { dao.observeByType("anime") } returns favFlow
        
        val result = repository.observeFavourites(MediaType.ANIME).first()
        assertThat(result).isEmpty()
    }

    @Test
    fun `observeFavourites emits items after Flow value changes`() = runTest {
        val favFlow = MutableStateFlow<List<FavouriteEntity>>(emptyList())
        every { dao.observeByType("anime") } returns favFlow
        
        val entity = FavouriteEntity(
            key = "anime-1",
            mediaType = "anime",
            mediaId = 1,
            title = "Test Anime",
            subtitle = "TV",
            score = "8.5"
        )
        
        favFlow.value = listOf(entity)
        repository.observeFavourites(MediaType.ANIME).test {
            val initial = awaitItem()
            assertThat(initial).hasSize(1)
            assertThat(initial[0].title).isEqualTo("Test Anime")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeIsFavourite emits false when not favourite`() = runTest {
        val existsFlow = MutableStateFlow(0)
        every { dao.observeIsFavourite("anime-1") } returns existsFlow
        
        val result = repository.observeIsFavourite(MediaType.ANIME, 1).first()
        assertThat(result).isFalse()
    }

    @Test
    fun `observeIsFavourite emits true when favourite`() = runTest {
        val existsFlow = MutableStateFlow(1)
        every { dao.observeIsFavourite("anime-1") } returns existsFlow
        
        val result = repository.observeIsFavourite(MediaType.ANIME, 1).first()
        assertThat(result).isTrue()
    }

    @Test
    fun `observeFavourites correctly maps multiple items from database`() = runTest {
        val favFlow = MutableStateFlow<List<FavouriteEntity>>(emptyList())
        every { dao.observeByType("anime") } returns favFlow
        
        val entities = listOf(
            FavouriteEntity("anime-1", "anime", 1, "Anime 1", "TV", "8.5"),
            FavouriteEntity("anime-2", "anime", 2, "Anime 2", "Movie", "9.0")
        )
        
        favFlow.value = entities
        val result = repository.observeFavourites(MediaType.ANIME).first()
        
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(1)
        assertThat(result[1].id).isEqualTo(2)
    }

    @Test
    fun `observeIsFavourite updates when Flow value changes`() = runTest {
        val existsFlow = MutableStateFlow(0)
        every { dao.observeIsFavourite("anime-1") } returns existsFlow
        
        var result = repository.observeIsFavourite(MediaType.ANIME, 1).first()
        assertThat(result).isFalse()
        
        existsFlow.value = 1
        result = repository.observeIsFavourite(MediaType.ANIME, 1).first()
        assertThat(result).isTrue()
    }
}
