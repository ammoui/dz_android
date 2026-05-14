package com.example.android_dz_manychkin_3.di

import android.content.Context
import androidx.room.Room
import com.example.android_dz_manychkin_3.data.local.AppDatabase
import com.example.android_dz_manychkin_3.data.local.FavouriteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app.db",
        ).build()
    }

    @Provides
    fun provideFavouriteDao(database: AppDatabase): FavouriteDao = database.favouriteDao()
}
