package com.example.travellog.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travellog.data.dao.PhotoDao
import com.example.travellog.data.entities.JournalEntryEntity
import com.example.travellog.data.entities.PhotoEntity
import com.example.travellog.data.repository.JournalRepository
import com.example.travellog.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for photo detail screen with swipe navigation.
 * Handles loading current photo, journal entry, and filtered photo list based on context.
 */
@HiltViewModel
class PhotoDetailViewModel @Inject constructor(
    private val photoDao: PhotoDao,
    private val journalRepository: JournalRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _currentPhotoId = MutableStateFlow<Long>(0)
    val currentPhotoId: StateFlow<Long> = _currentPhotoId.asStateFlow()

    private val _contextType = MutableStateFlow<String>("all")
    private val _contextValue = MutableStateFlow<String>("")

    /**
     * Current photo observable.
     * Automatically updates when currentPhotoId changes.
     */
    val currentPhoto: StateFlow<PhotoEntity?> = _currentPhotoId
        .flatMapLatest { photoId ->
            photoDao.getById(photoId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Journal entry for current photo.
     * Automatically updates when currentPhotoId changes.
     */
    val currentJournal: StateFlow<JournalEntryEntity?> = _currentPhotoId
        .flatMapLatest { photoId ->
            journalRepository.getJournalByPhotoId(photoId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _photoList = MutableStateFlow<List<PhotoEntity>>(emptyList())
    val photoList: StateFlow<List<PhotoEntity>> = _photoList.asStateFlow()

    /**
     * Initialize the view model with photo ID and context.
     * Loads the appropriate photo list based on context type.
     *
     * @param photoId Initial photo ID to display
     * @param contextType Type of context: "state", "city", or "all"
     * @param contextValue Value for context (state code or city name)
     */
    fun initialize(photoId: Long, contextType: String, contextValue: String) {
        Log.d("PhotoDetailVM", "════════════════════════════════════")
        Log.d("PhotoDetailVM", "INITIALIZE called")
        Log.d("PhotoDetailVM", "  photoId: $photoId")
        Log.d("PhotoDetailVM", "  contextType: '$contextType'")
        Log.d("PhotoDetailVM", "  contextValue: '$contextValue' (length=${contextValue.length})")
        Log.d("PhotoDetailVM", "════════════════════════════════════")

        _currentPhotoId.value = photoId
        _contextType.value = contextType
        _contextValue.value = contextValue

        // Load appropriate photo list based on context
        viewModelScope.launch {
            val flow = when (contextType) {
                "state" -> {
                    Log.d("PhotoDetailVM", "Filtering by STATE: '$contextValue'")
                    photoDao.getByState(contextValue)
                }
                "city" -> {
                    Log.d("PhotoDetailVM", "Filtering by CITY: '$contextValue'")
                    photoDao.getByCity(contextValue)
                }
                else -> {
                    Log.d("PhotoDetailVM", "Loading ALL photos (no filter)")
                    photoDao.getAll()
                }
            }

            flow.collect { photos ->
                Log.d("PhotoDetailVM", "Photo list updated: ${photos.size} photos")
                photos.forEachIndexed { index, photo ->
                    Log.d("PhotoDetailVM", "  [$index] Photo ${photo.id}: ${photo.cityName.trim()}, ${photo.stateCode}")
                }
                _photoList.value = photos
            }
        }
    }

    /**
     * Update current photo ID.
     * Used when swiping between photos in HorizontalPager.
     * @param photoId New photo ID to display
     */
    fun updateCurrentPhoto(photoId: Long) {
        _currentPhotoId.value = photoId
    }

    /**
     * Navigate to next photo in the list.
     * Does nothing if already at last photo.
     */
    fun navigateToNextPhoto() {
        val photos = _photoList.value
        val currentIndex = photos.indexOfFirst { it.id == _currentPhotoId.value }
        if (currentIndex >= 0 && currentIndex < photos.size - 1) {
            _currentPhotoId.value = photos[currentIndex + 1].id
        }
    }

    /**
     * Navigate to previous photo in the list.
     * Does nothing if already at first photo.
     */
    fun navigateToPreviousPhoto() {
        val photos = _photoList.value
        val currentIndex = photos.indexOfFirst { it.id == _currentPhotoId.value }
        if (currentIndex > 0) {
            _currentPhotoId.value = photos[currentIndex - 1].id
        }
    }

    /**
     * Delete current photo.
     * Cascade delete will automatically remove associated journal entry.
     * Caller should navigate back after calling this method.
     */
    fun deleteCurrentPhoto() {
        viewModelScope.launch {
            photoRepository.deletePhoto(_currentPhotoId.value)
        }
    }

    /**
     * Get current photo index in the list.
     * @return Index of current photo, or -1 if not found
     */
    fun getCurrentPhotoIndex(): Int {
        val photos = _photoList.value
        return photos.indexOfFirst { it.id == _currentPhotoId.value }
    }

    /**
     * Check if there is a next photo available.
     * @return True if not at last photo
     */
    fun hasNextPhoto(): Boolean {
        val currentIndex = getCurrentPhotoIndex()
        return currentIndex >= 0 && currentIndex < _photoList.value.size - 1
    }

    /**
     * Check if there is a previous photo available.
     * @return True if not at first photo
     */
    fun hasPreviousPhoto(): Boolean {
        val currentIndex = getCurrentPhotoIndex()
        return currentIndex > 0
    }
}
