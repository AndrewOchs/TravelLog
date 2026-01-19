package com.example.wanderstate.di

import android.content.Context
import androidx.room.Room
import com.example.wanderstate.data.dao.JournalEntryDao
import com.example.wanderstate.data.dao.PhotoDao
import com.example.wanderstate.data.database.WanderStateDatabase
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
    fun provideWanderStateDatabase(
        @ApplicationContext context: Context
    ): WanderStateDatabase {
        return Room.databaseBuilder(
            context,
            WanderStateDatabase::class.java,
            WanderStateDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Remove in production
            .build()
    }

    @Provides
    @Singleton
    fun providePhotoDao(database: WanderStateDatabase): PhotoDao {
        return database.photoDao()
    }

    @Provides
    @Singleton
    fun provideJournalEntryDao(database: WanderStateDatabase): JournalEntryDao {
        return database.journalEntryDao()
    }
}
