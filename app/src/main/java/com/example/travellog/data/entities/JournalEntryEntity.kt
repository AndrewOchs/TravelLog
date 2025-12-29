package com.example.travellog.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "journal_entries",
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photo_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["photo_id"])]
)
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "photo_id")
    val photoId: Long,

    @ColumnInfo(name = "entry_text")
    val entryText: String,

    @ColumnInfo(name = "created_date")
    val createdDate: Long,

    @ColumnInfo(name = "updated_date")
    val updatedDate: Long
)
