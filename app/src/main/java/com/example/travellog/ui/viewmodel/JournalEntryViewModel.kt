package com.example.travellog.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travellog.data.dao.PhotoDao
import com.example.travellog.data.entities.JournalEntryEntity
import com.example.travellog.data.entities.PhotoEntity
import com.example.travellog.data.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for journal entry creation and editing screen.
 * Handles loading photo, existing journal entry, and saving journal entries.
 */
@HiltViewModel
class JournalEntryViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val photoDao: PhotoDao
) : ViewModel() {

    private val _saveState = MutableStateFlow<JournalSaveState>(JournalSaveState.Idle)
    val saveState: StateFlow<JournalSaveState> = _saveState.asStateFlow()

    // Guard against multiple save calls - survives recomposition
    private var isSaving = false

    /**
     * Get photo by ID as StateFlow.
     * @param photoId The photo ID to retrieve
     * @return StateFlow emitting the photo or null if not found
     */
    fun getPhoto(photoId: Long): StateFlow<PhotoEntity?> {
        return photoDao.getById(photoId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    /**
     * Get journal entry for a photo as StateFlow.
     * @param photoId The photo ID to get journal for
     * @return StateFlow emitting the journal entry or null if none exists
     */
    fun getJournalForPhoto(photoId: Long): StateFlow<JournalEntryEntity?> {
        return journalRepository.getJournalByPhotoId(photoId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    /**
     * Save or update journal entry for a photo.
     * Automatically handles create vs update based on existing entry.
     * Calls onComplete callback after successful save (for navigation).
     *
     * @param photoId The photo ID to save journal for
     * @param entryText The journal entry text
     * @param onComplete Callback invoked after successful save
     */
    fun saveJournal(photoId: Long, entryText: String, onComplete: () -> Unit = {}) {
        // Guard against multiple save calls - prevent navigation loop
        if (isSaving) {
            Log.d("JournalSave", "ALREADY saving - ignoring duplicate call")
            return
        }

        Log.d("JournalSave", "Setting isSaving guard and starting save")
        isSaving = true

        viewModelScope.launch {
            _saveState.value = JournalSaveState.Saving

            val result = journalRepository.saveJournal(photoId, entryText)

            _saveState.value = result.fold(
                onSuccess = { journalId ->
                    Log.d("JournalSave", "Save successful - calling onComplete")
                    // Call completion callback AFTER state is updated
                    onComplete()
                    // Don't reset isSaving - let it stay true to prevent re-saves
                    JournalSaveState.Success(journalId)
                },
                onFailure = { error ->
                    Log.e("JournalSave", "Save failed: ${error.message}")
                    // Reset isSaving on error so user can retry
                    isSaving = false
                    JournalSaveState.Error(error.message ?: "Failed to save journal entry")
                }
            )
        }
    }

    /**
     * Reset save state back to Idle.
     * Call this when navigating away or after handling save success/error.
     */
    fun resetSaveState() {
        _saveState.value = JournalSaveState.Idle
    }
}

/**
 * Sealed class representing the state of journal save operation.
 */
sealed class JournalSaveState {
    /** Initial idle state, no save operation in progress */
    data object Idle : JournalSaveState()

    /** Save operation is in progress */
    data object Saving : JournalSaveState()

    /** Save operation completed successfully */
    data class Success(val journalId: Long) : JournalSaveState()

    /** Save operation failed with error message */
    data class Error(val message: String) : JournalSaveState()
}
