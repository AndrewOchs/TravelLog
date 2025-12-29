package com.example.travellog.di

import android.content.Context
import androidx.room.Room
import com.example.travellog.data.dao.JournalEntryDao
import com.example.travellog.data.dao.PhotoDao
import com.example.travellog.data.database.TravelLogDatabase
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
    fun provideTravelLogDatabase(
        @ApplicationContext context: Context
    ): TravelLogDatabase {
        return Room.databaseBuilder(
            context,
            TravelLogDatabase::class.java,
            TravelLogDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Remove in production
            .build()
    }

    @Provides
    @Singleton
    fun providePhotoDao(database: TravelLogDatabase): PhotoDao {
        return database.photoDao()
    }

    @Provides
    @Singleton
    fun provideJournalEntryDao(database: TravelLogDatabase): JournalEntryDao {
        return database.journalEntryDao()
    }
}
