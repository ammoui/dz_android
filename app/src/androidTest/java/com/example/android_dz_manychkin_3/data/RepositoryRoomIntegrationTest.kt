package com.example.android_dz_manychkin_3.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.android_dz_manychkin_3.data.local.AppDatabase
import com.example.android_dz_manychkin_3.data.local.FavouriteEntity
import com.example.android_dz_manychkin_3.model.MediaType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class RepositoryRoomIntegrationTest {
    
    private lateinit var db: AppDatabase
    private lateinit var dao: com.example.android_dz_manychkin_3.data.local.FavouriteDao
    private val animeEntity = FavouriteEntity(
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
        synopsis = "Test synopsis"
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.favouriteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun shouldSaveAndRetrieveFavouriteItemFromRoom() = runTest {
        dao.upsert(animeEntity)
        
        val retrieved = dao.getByKey("anime-1")
        
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.title).isEqualTo("Test Anime")
        assertThat(retrieved?.score).isEqualTo("8.5")
        assertThat(retrieved?.format).isEqualTo("TV")
    }

    @Test
    fun shouldObserveItemsByType() = runTest {
        dao.upsert(animeEntity)
        dao.upsert(animeEntity.copy(key = "anime-2", mediaId = 2, title = "Another Anime"))
        
        val items = dao.observeByType("anime").first()
        
        assertThat(items).hasSize(2)
        assertThat(items.map { it.mediaId }).containsExactly(1, 2)
    }

    @Test
    fun shouldDeleteFavouriteItem() = runTest {
        dao.upsert(animeEntity)
        assertThat(dao.getByKey("anime-1")).isNotNull()
        
        dao.deleteByKey("anime-1")
        
        assertThat(dao.getByKey("anime-1")).isNull()
    }

    @Test
    fun shouldNotCreateDuplicatesOnRepeatedInsert() = runTest {
        dao.upsert(animeEntity)
        dao.upsert(animeEntity) // Insert same item again
        
        val items = dao.observeByType("anime").first()
        
        assertThat(items).hasSize(1)
    }

    @Test
    fun shouldObserveExistenceOfItem() = runTest {
        dao.upsert(animeEntity)
        
        val exists = dao.observeIsFavourite("anime-1").first()
        val notExists = dao.observeIsFavourite("anime-999").first()
        
        assertThat(exists).isEqualTo(1)
        assertThat(notExists).isEqualTo(0)
    }

    @Test
    fun shouldHandleDifferentMediaTypesSeparately() = runTest {
        val mangaEntity = animeEntity.copy(
            key = "manga-1",
            mediaType = "manga",
            mediaId = 1
        )
        
        dao.upsert(animeEntity)
        dao.upsert(mangaEntity)
        
        val animeItems = dao.observeByType("anime").first()
        val mangaItems = dao.observeByType("manga").first()
        
        assertThat(animeItems).hasSize(1)
        assertThat(animeItems[0].mediaType).isEqualTo("anime")
        
        assertThat(mangaItems).hasSize(1)
        assertThat(mangaItems[0].mediaType).isEqualTo("manga")
    }
}
