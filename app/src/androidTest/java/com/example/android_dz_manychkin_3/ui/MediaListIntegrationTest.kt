package com.example.android_dz_manychkin_3.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import com.example.android_dz_manychkin_3.data.JikanRepository
import com.example.android_dz_manychkin_3.model.MediaListItem
import com.example.android_dz_manychkin_3.model.MediaType
import com.example.android_dz_manychkin_3.ui.list.MediaListEvent
import com.example.android_dz_manychkin_3.ui.list.MediaListScreen
import com.example.android_dz_manychkin_3.ui.list.MediaListViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class MediaListIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: JikanRepository
    private lateinit var viewModel: MediaListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        coEvery { repository.observeFavourites(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testErrorToRetryToSuccessIntegration() {
        // Step 1: Initial load fails
        coEvery { repository.loadMediaList(MediaType.ANIME, "") } throws IOException("Network error")
        
        viewModel = MediaListViewModel(repository)

        composeTestRule.setContent {
            MediaListScreen(
                uiState = viewModel.uiState,
                onEvent = { viewModel.onEvent(it) },
                onOpenDetail = { _, _ -> }
            )
        }

        // Verify error is displayed
        composeTestRule.onNodeWithText("Проблема с интернетом", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()

        // Step 2: Setup successful response for retry
        val successData = listOf(
            MediaListItem(id = 1, mediaType = MediaType.ANIME, title = "Naruto", subtitle = "TV", score = "8.5")
        )
        coEvery { repository.loadMediaList(MediaType.ANIME, "") } returns successData

        // Step 3: Perform click on Retry
        composeTestRule.onNodeWithText("Retry").performClick()



        // Step 4: Verify success state is displayed
        composeTestRule.onNodeWithText("Naruto").assertIsDisplayed()
        
        // Verify repository was called twice
        coVerify(exactly = 2) { repository.loadMediaList(MediaType.ANIME, "") }
    }
}
