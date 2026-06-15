package com.example.android_dz_manychkin_3.data

import app.cash.turbine.test
import com.example.android_dz_manychkin_3.data.local.FavouriteDao
import com.example.android_dz_manychkin_3.data.local.FavouriteEntity
import com.example.android_dz_manychkin_3.data.remote.JikanApi
import com.example.android_dz_manychkin_3.model.MediaType
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class JikanRepositoryFlowTest {

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
    fun `observeFavourites emits full sequence of changes correctly`() = runTest {
        val favFlow = MutableStateFlow<List<FavouriteEntity>>(emptyList())
        every { dao.observeByType("anime") } returns favFlow

        repository.observeFavourites(MediaType.ANIME).test {
            // Initial emission (empty)
            val initial = awaitItem()
            assertThat(initial).isEmpty()

            // State changes - 1 item added
            val entity1 = FavouriteEntity("anime-1", "anime", 1, "Anime 1", "TV", "8.5")
            favFlow.value = listOf(entity1)
            val firstChange = awaitItem()
            assertThat(firstChange).hasSize(1)
            assertThat(firstChange[0].title).isEqualTo("Anime 1")

            // State changes - another item added
            val entity2 = FavouriteEntity("anime-2", "anime", 2, "Anime 2", "TV", "9.0")
            favFlow.value = listOf(entity1, entity2)
            val secondChange = awaitItem()
            assertThat(secondChange).hasSize(2)
            assertThat(secondChange[1].title).isEqualTo("Anime 2")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeIsFavourite handles boolean state transitions correctly`() = runTest {
        val existsFlow = MutableStateFlow(0)
        every { dao.observeIsFavourite("anime-1") } returns existsFlow

        repository.observeIsFavourite(MediaType.ANIME, 1).test {
            // Initial emission (not favourite)
            assertThat(awaitItem()).isFalse()

            // Added to favourites
            existsFlow.value = 1
            assertThat(awaitItem()).isTrue()

            // Removed from favourites
            existsFlow.value = 0
            assertThat(awaitItem()).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeIsFavourite does not emit duplicate identical values`() = runTest {
        val existsFlow = MutableStateFlow(0)
        every { dao.observeIsFavourite("anime-1") } returns existsFlow

        repository.observeIsFavourite(MediaType.ANIME, 1).test {
            // First emission
            assertThat(awaitItem()).isFalse()

            // Emit the same underlying value
            existsFlow.value = 0
            
            // Should not emit anything new, so we expect no events
            expectNoEvents()

            // Now emit a different value to prove it still works
            existsFlow.value = 1
            assertThat(awaitItem()).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }
}
