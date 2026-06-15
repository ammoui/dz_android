package com.example.android_dz_manychkin_3.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.android_dz_manychkin_3.data.local.AppDatabase
import com.example.android_dz_manychkin_3.data.local.FavouriteEntity
import com.example.android_dz_manychkin_3.model.MediaDetail
import com.example.android_dz_manychkin_3.model.MediaType
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery

class FavouriteToggleBehaviorTest {
    
    private lateinit var db: AppDatabase
    private lateinit var dao: com.example.android_dz_manychkin_3.data.local.FavouriteDao
    private lateinit var api: com.example.android_dz_manychkin_3.data.remote.JikanApi
    private lateinit var repository: JikanRepository
    
    private val testDetail = MediaDetail(
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

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.favouriteDao()
        api = mockk(relaxed = true)
        repository = JikanRepository(api, dao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addingToFavouritesTwiceShouldNotCreateDuplicates() = runTest {
        repository.setFavourite(testDetail, true)
        repository.setFavourite(testDetail, true)
        
        val items = dao.observeByType("anime").first()
        
        assertThat(items).hasSize(1)
        assertThat(items[0].mediaId).isEqualTo(1)
    }

    @Test
    fun toggleFavouriteOnThenOffShouldRemoveItem() = runTest {
        repository.setFavourite(testDetail, true)
        assertThat(dao.getByKey("anime-1")).isNotNull()
        
        repository.setFavourite(testDetail, false)
        
        assertThat(dao.getByKey("anime-1")).isNull()
    }

    @Test
    fun favouriteToggleSequenceShouldPreserveDataIntegrity() = runTest {
        repository.setFavourite(testDetail, true)
        var saved = dao.getByKey("anime-1")
        assertThat(saved?.title).isEqualTo("Test Anime")
        
        repository.setFavourite(testDetail, false)
        assertThat(dao.getByKey("anime-1")).isNull()
        
        repository.setFavourite(testDetail, true)
        saved = dao.getByKey("anime-1")
        assertThat(saved?.title).isEqualTo("Test Anime")
        assertThat(saved?.score).isEqualTo("8.5")
    }

    @Test
    fun updatingFavouriteDetailsShouldReflectInRoom() = runTest {
        val original = testDetail
        repository.setFavourite(original, true)
        
        var saved = dao.getByKey("anime-1")
        assertThat(saved?.score).isEqualTo("8.5")
        
        val updated = original.copy(score = "9.0")
        repository.setFavourite(updated, true)
        
        saved = dao.getByKey("anime-1")
        assertThat(saved?.score).isEqualTo("9.0")
    }

    @Test
    fun multipleMediaItemsCanBeFavouritesSimultaneously() = runTest {
        val detail1 = testDetail
        val detail2 = testDetail.copy(id = 2, title = "Another Anime")
        
        repository.setFavourite(detail1, true)
        repository.setFavourite(detail2, true)
        
        val items = dao.observeByType("anime").first()
        
        assertThat(items).hasSize(2)
        assertThat(items.map { it.mediaId }).containsExactly(1, 2)
    }
}
