package com.example.android_dz_manychkin_3.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {
    @Query("SELECT * FROM favourites WHERE mediaType = :mediaType ORDER BY title")
    fun observeByType(mediaType: String): Flow<List<FavouriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE key = :key)")
    fun observeIsFavourite(key: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE key = :key")
    suspend fun deleteByKey(key: String)
}
