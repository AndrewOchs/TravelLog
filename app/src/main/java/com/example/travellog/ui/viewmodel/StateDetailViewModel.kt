package com.example.travellog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travellog.data.dao.JournalEntryDao
import com.example.travellog.data.dao.PhotoDao
import com.example.travellog.data.models.PhotoWithJournalInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StateDetailViewModel @Inject constructor(
    private val photoDao: PhotoDao,
    private val journalEntryDao: JournalEntryDao
) : ViewModel() {

    /**
     * Get photos for a specific state with journal status.
     * Combines photo list with journal status efficiently using batch query.
     *
     * @param stateCode The state code to filter photos by
     * @return StateFlow emitting list of photos with journal status for the state
     */
    fun getPhotosWithJournalForState(stateCode: String): StateFlow<List<PhotoWithJournalInfo>> {
        return photoDao.getByState(stateCode)
            .combine(journalEntryDao.getAllPhotoIdsWithJournals()) { photos, journalPhotoIds ->
                photos.map { photo ->
                    PhotoWithJournalInfo(
                        photo = photo,
                        hasJournal = journalPhotoIds.contains(photo.id)
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}
