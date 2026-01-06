package com.example.travellog.di

import com.example.travellog.data.repository.JournalRepository
import com.example.travellog.data.repository.JournalRepositoryImpl
import com.example.travellog.data.repository.PhotoRepository
import com.example.travellog.data.repository.PhotoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        photoRepositoryImpl: PhotoRepositoryImpl
    ): PhotoRepository

    @Binds
    @Singleton
    abstract fun bindJournalRepository(
        journalRepositoryImpl: JournalRepositoryImpl
    ): JournalRepository
}
