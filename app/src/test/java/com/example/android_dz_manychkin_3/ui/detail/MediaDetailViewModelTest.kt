package com.example.android_dz_manychkin_3.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.example.android_dz_manychkin_3.data.JikanRepository
import com.example.android_dz_manychkin_3.model.MediaDetail
import com.example.android_dz_manychkin_3.model.MediaType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class MediaDetailViewModelTest {

    private lateinit var repository: JikanRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        savedStateHandle = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initializes correctly and loads data with valid arguments`() = runTest {
        every { savedStateHandle.get<String>("mediaType") } returns "anime"
        every { savedStateHandle.get<Int>("id") } returns 1
        
        val detail = MediaDetail(1, MediaType.ANIME, "Title", "TV", "2024", "10.0", "Finished", "24 ep", "Synopsis")
        coEvery { repository.loadMediaDetailOrFavourite(MediaType.ANIME, 1) } returns detail
        coEvery { repository.observeIsFavourite(MediaType.ANIME, 1) } returns flowOf(true)

        val viewModel = MediaDetailViewModel(savedStateHandle, repository)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertThat(state).isInstanceOf(MediaDetailUiState.Content::class.java)
        state as MediaDetailUiState.Content
        assertThat(state.detail).isEqualTo(detail)
        assertThat(state.isFavourite).isTrue()
    }

    @Test
    fun `initializes with error if arguments are invalid`() = runTest {
        every { savedStateHandle.get<String>("mediaType") } returns null
        every { savedStateHandle.get<Int>("id") } returns null

        val viewModel = MediaDetailViewModel(savedStateHandle, repository)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertThat(state).isInstanceOf(MediaDetailUiState.Error::class.java)
        assertThat((state as MediaDetailUiState.Error).message).isEqualTo("Ошибка аргументов навигации")
    }

    @Test
    fun `toggleFavourite updates repository correctly`() = runTest {
        every { savedStateHandle.get<String>("mediaType") } returns "anime"
        every { savedStateHandle.get<Int>("id") } returns 1
        
        val detail = MediaDetail(1, MediaType.ANIME, "Title", "TV", "2024", "10.0", "Finished", "24 ep", "Synopsis")
        coEvery { repository.loadMediaDetailOrFavourite(MediaType.ANIME, 1) } returns detail
        // Initially not favourite
        coEvery { repository.observeIsFavourite(MediaType.ANIME, 1) } returns flowOf(false)

        val viewModel = MediaDetailViewModel(savedStateHandle, repository)
        advanceUntilIdle()
        
        // Toggle from false to true
        viewModel.toggleFavourite()
        advanceUntilIdle()
        
        coVerify { repository.setFavourite(detail, true) }
    }
}
