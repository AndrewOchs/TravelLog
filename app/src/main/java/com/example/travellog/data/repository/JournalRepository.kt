package com.example.travellog.data.repository

import com.example.travellog.data.dao.JournalEntryDao
import com.example.travellog.data.entities.JournalEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for journal entry operations.
 * Provides abstraction over data sources for journal entries.
 */
interface JournalRepository {
    /**
     * Get journal entry for a specific photo.
     * @param photoId The photo ID to get journal for
     * @return Flow emitting journal entry or null if none exists
     */
    fun getJournalByPhotoId(photoId: Long): Flow<JournalEntryEntity?>

    /**
     * Save or update journal entry for a photo (1:1 relationship).
     * If journal already exists, updates it. Otherwise creates new entry.
     * @param photoId The photo ID to save journal for
     * @param entryText The journal entry text
     * @return Result with journal entry ID on success, or error on failure
     */
    suspend fun saveJournal(photoId: Long, entryText: String): Result<Long>

    /**
     * Delete a journal entry by ID.
     * @param journalId The journal entry ID to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteJournal(journalId: Long): Result<Unit>
}

/**
 * Implementation of JournalRepository using Room database.
 */
@Singleton
class JournalRepositoryImpl @Inject constructor(
    private val journalEntryDao: JournalEntryDao
) : JournalRepository {

    override fun getJournalByPhotoId(photoId: Long): Flow<JournalEntryEntity?> {
        return journalEntryDao.getByPhotoId(photoId).map { entries ->
            // Since we enforce 1:1 relationship, return first entry or null
            entries.firstOrNull()
        }
    }

    override suspend fun saveJournal(photoId: Long, entryText: String): Result<Long> =
        withContext(Dispatchers.IO) {
            try {
                // Check if journal entry already exists for this photo (1:1 relationship)
                val existingEntries = journalEntryDao.getByPhotoId(photoId).first()
                val existing = existingEntries.firstOrNull()

                if (existing != null) {
                    // Update existing journal entry
                    val updated = existing.copy(
                        entryText = entryText,
                        updatedDate = System.currentTimeMillis()
                    )
                    journalEntryDao.update(updated)
                    Result.success(existing.id)
                } else {
                    // Create new journal entry
                    val currentTime = System.currentTimeMillis()
                    val newEntry = JournalEntryEntity(
                        id = 0, // Room will auto-generate
                        photoId = photoId,
                        entryText = entryText,
                        createdDate = currentTime,
                        updatedDate = currentTime
                    )
                    val id = journalEntryDao.insert(newEntry)
                    Result.success(id)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteJournal(journalId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                journalEntryDao.deleteById(journalId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
