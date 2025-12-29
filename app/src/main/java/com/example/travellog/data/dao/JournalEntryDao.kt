package com.example.travellog.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.travellog.data.entities.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<JournalEntryEntity>): List<Long>

    @Update
    suspend fun update(entry: JournalEntryEntity)

    @Delete
    suspend fun delete(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)

    @Query("DELETE FROM journal_entries WHERE photo_id = :photoId")
    suspend fun deleteByPhotoId(photoId: Long)

    @Query("SELECT * FROM journal_entries WHERE id = :entryId")
    fun getById(entryId: Long): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE photo_id = :photoId ORDER BY created_date DESC")
    fun getByPhotoId(photoId: Long): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries ORDER BY created_date DESC")
    fun getAll(): Flow<List<JournalEntryEntity>>

    @Query("SELECT COUNT(*) FROM journal_entries WHERE photo_id = :photoId")
    fun getEntryCountByPhotoId(photoId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM journal_entries")
    fun getTotalEntryCount(): Flow<Int>
}
