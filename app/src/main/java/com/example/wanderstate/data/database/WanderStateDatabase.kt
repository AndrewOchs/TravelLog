package com.example.wanderstate.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wanderstate.data.dao.JournalEntryDao
import com.example.wanderstate.data.dao.PhotoDao
import com.example.wanderstate.data.entities.JournalEntryEntity
import com.example.wanderstate.data.entities.PhotoEntity

@Database(
    entities = [
        PhotoEntity::class,
        JournalEntryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WanderStateDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao
    abstract fun journalEntryDao(): JournalEntryDao

    companion object {
        const val DATABASE_NAME = "wanderstate_database"
    }
}
