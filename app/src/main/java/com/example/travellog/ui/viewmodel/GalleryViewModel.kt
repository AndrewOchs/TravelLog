package com.example.travellog.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travellog.data.dao.JournalEntryDao
import com.example.travellog.data.dao.PhotoDao
import com.example.travellog.data.models.PhotoWithJournalInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val TAG = "GalleryViewModel"

/**
 * Gallery filter types for organizing photos
 */
enum class GalleryFilter {
    BY_STATE,
    BY_DATE,
    BY_CITY
}

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val photoDao: PhotoDao,
    private val journalEntryDao: JournalEntryDao
) : ViewModel() {

    // Filter state - survives navigation
    private val _currentFilter = MutableStateFlow(GalleryFilter.BY_STATE)
    val currentFilter: StateFlow<GalleryFilter> = _currentFilter.asStateFlow()

    /**
     * All photos with journal status.
     * Combines photo list with journal status efficiently using batch query.
     */
    val allPhotosWithJournalStatus: StateFlow<List<PhotoWithJournalInfo>> =
        photoDao.getAll()
            .combine(journalEntryDao.getAllPhotoIdsWithJournals()) { photos, journalPhotoIds ->
                photos.map { photo ->
                    PhotoWithJournalInfo(
                        photo = photo,
                        hasJournal = journalPhotoIds.contains(photo.id)
                    )
                }
            }
            .onEach { photosWithJournal ->
                Log.d(TAG, "═══════════════════════════════════")
                Log.d(TAG, "GALLERY - Photos with journal status")
                Log.d(TAG, "Total photos: ${photosWithJournal.size}")
                Log.d(TAG, "Photos with journals: ${photosWithJournal.count { it.hasJournal }}")
                Log.d(TAG, "═══════════════════════════════════")
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Update the current filter selection.
     * Filter state persists across navigation.
     */
    fun setFilter(filter: GalleryFilter) {
        Log.d(TAG, "Changing filter to: $filter")
        _currentFilter.value = filter
    }
}
