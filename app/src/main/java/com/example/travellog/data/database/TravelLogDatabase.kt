package com.example.travellog.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.travellog.data.dao.JournalEntryDao
import com.example.travellog.data.dao.PhotoDao
import com.example.travellog.data.entities.JournalEntryEntity
import com.example.travellog.data.entities.PhotoEntity

@Database(
    entities = [
        PhotoEntity::class,
        JournalEntryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TravelLogDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao
    abstract fun journalEntryDao(): JournalEntryDao

    companion object {
        const val DATABASE_NAME = "travellog_database"
    }
}
