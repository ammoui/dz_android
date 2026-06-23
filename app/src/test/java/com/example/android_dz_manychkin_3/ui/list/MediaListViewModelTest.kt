package com.example.android_dz_manychkin_3.ui.list

import com.example.android_dz_manychkin_3.data.JikanRepository
import com.example.android_dz_manychkin_3.model.MediaListItem
import com.example.android_dz_manychkin_3.model.MediaType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class MediaListViewModelTest {
    
    private lateinit var repository: JikanRepository
    private lateinit var viewModel: MediaListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        // Mock observeFavourites to return empty list by default
        coEvery { repository.observeFavourites(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() = runTest {
        coEvery { repository.loadMediaList(MediaType.ANIME, "") } returns emptyList()
        
        viewModel = MediaListViewModel(repository)
        
        assertThat(viewModel.uiState.query).isEmpty()
        assertThat(viewModel.uiState.mediaType).isEqualTo(MediaType.ANIME)
        assertThat(viewModel.uiState.items).isEmpty()
        assertThat(viewModel.uiState.isLoading).isFalse()
    }

    @Test
    fun `should load media list successfully`() = runTest {
        val items = listOf(
            MediaListItem(id = 1, mediaType = MediaType.ANIME, title = "Test Anime", subtitle = "2024", score = "8.5"),
            MediaListItem(id = 2, mediaType = MediaType.ANIME, title = "Test Anime 2", subtitle = "2024", score = "7.5")
        )
        coEvery { repository.loadMediaList(MediaType.ANIME, "") } returns items
        
        viewModel = MediaListViewModel(repository)
        advanceUntilIdle()
        
        assertThat(viewModel.uiState.items).isEqualTo(items)
        assertThat(viewModel.uiState.isLoading).isFalse()
        assertThat(viewModel.uiState.errorMessage).isNull()
    }

    @Test
    fun `error should update state with error message`() = runTest {
        coEvery { repository.loadMediaList(MediaType.ANIME, "") } throws IOException("Network Error")
        
        viewModel = MediaListViewModel(repository)
        advanceUntilIdle()
        
        assertThat(viewModel.uiState.isLoading).isFalse()
        assertThat(viewModel.uiState.errorMessage).isEqualTo("Проблема с интернетом. Проверьте соединение.")
    }

    @Test
    fun `retry should reload data after error`() = runTest {
        coEvery { repository.loadMediaList(any(), any()) } throws IOException("Error")
        viewModel = MediaListViewModel(repository)
        advanceUntilIdle()
        assertThat(viewModel.uiState.errorMessage).isNotNull()
        
        val items = listOf(
            MediaListItem(id = 1, mediaType = MediaType.ANIME, title = "Test", subtitle = "2024", score = "8.5")
        )
        coEvery { repository.loadMediaList(any(), any()) } returns items
        
        viewModel.onEvent(MediaListEvent.Retry)
        advanceUntilIdle()
        
        assertThat(viewModel.uiState.items).isEqualTo(items)
        assertThat(viewModel.uiState.errorMessage).isNull()
        coVerify(exactly = 2) { repository.loadMediaList(any(), any()) }
    }

    @Test
    fun `debounce cancels old request when query changes fast`() = runTest {
        coEvery { repository.loadMediaList(any(), any()) } returns emptyList()
        viewModel = MediaListViewModel(repository)
        advanceUntilIdle()
        
        viewModel.onEvent(MediaListEvent.QueryChanged("a"))
        advanceTimeBy(100) // Not enough to pass debounce (350ms)
        
        viewModel.onEvent(MediaListEvent.QueryChanged("ab"))
        advanceTimeBy(100)
        
        viewModel.onEvent(MediaListEvent.QueryChanged("abc"))
        advanceUntilIdle() // Now wait for everything to finish
        
        // Ensure that loadMediaList is only called for the initial load and the final query "abc"
        // (the "a" and "ab" queries were cancelled before delay finished)
        coVerify(exactly = 1) { repository.loadMediaList(MediaType.ANIME, "abc") }
        coVerify(exactly = 0) { repository.loadMediaList(MediaType.ANIME, "a") }
        coVerify(exactly = 0) { repository.loadMediaList(MediaType.ANIME, "ab") }
    }
}
