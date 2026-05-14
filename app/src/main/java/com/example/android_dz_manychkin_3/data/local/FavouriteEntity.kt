package com.example.android_dz_manychkin_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey val key: String,
    val mediaType: String,
    val mediaId: Int,
    val title: String,
)
