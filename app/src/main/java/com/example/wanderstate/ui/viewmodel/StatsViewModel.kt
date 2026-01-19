package com.example.wanderstate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderstate.data.dao.JournalEntryDao
import com.example.wanderstate.data.dao.PhotoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Data class for state statistics.
 */
data class StateStats(
    val stateCode: String,
    val stateName: String,
    val photoCount: Int
)

/**
 * Data class for overall statistics.
 */
data class TravelStats(
    val statesVisited: Int,
    val totalStates: Int = 50,
    val totalPhotos: Int,
    val photosWithJournals: Int,
    val journalPercentage: Float,
    val mostPhotographedState: StateStats?,
    val daysSinceFirstPhoto: Int,
    val photosThisWeek: Int,
    val photosThisMonth: Int,
    val statesThisMonth: Int,
    val averagePhotosPerState: Float,
    val longestJournalLength: Int,
    val stateBreakdown: List<StateStats>
)

/**
 * ViewModel for Statistics screen.
 * Calculates and provides travel statistics from photo and journal data.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val photoDao: PhotoDao,
    private val journalEntryDao: JournalEntryDao
) : ViewModel() {

    /**
     * Complete travel statistics combining all metrics.
     */
    val travelStats: StateFlow<TravelStats> = combine(
        photoDao.getAll(),
        journalEntryDao.getAll()
    ) { photos, journals ->
        // States visited (unique state codes)
        val statesVisited = photos.map { it.stateCode }.distinct().size

        // Total photos
        val totalPhotos = photos.size

        // Photos with journals
        val photoIdsWithJournals = journals.map { it.photoId }.toSet()
        val photosWithJournals = photos.count { photoIdsWithJournals.contains(it.id) }
        val journalPercentage = if (totalPhotos > 0) {
            (photosWithJournals.toFloat() / totalPhotos) * 100
        } else 0f

        // Most photographed state
        val statePhotoCounts = photos.groupBy { it.stateCode }
            .map { (stateCode, statePhotos) ->
                StateStats(
                    stateCode = stateCode,
                    stateName = statePhotos.first().stateName,
                    photoCount = statePhotos.size
                )
            }
            .sortedByDescending { it.photoCount }

        val mostPhotographedState = statePhotoCounts.firstOrNull()

        // Days since first photo was added to the app
        val firstPhoto = photos.minByOrNull { it.addedDate }
        val daysSinceFirstPhoto = if (firstPhoto != null) {
            val daysDiff = System.currentTimeMillis() - firstPhoto.addedDate
            TimeUnit.MILLISECONDS.toDays(daysDiff).toInt()
        } else 0

        // Photos this week (last 7 days)
        val oneWeekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        val photosThisWeek = photos.count { it.capturedDate >= oneWeekAgo }

        // Photos this month
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val firstDayOfMonth = calendar.timeInMillis

        val photosThisMonth = photos.count { it.capturedDate >= firstDayOfMonth }

        // States visited this month
        val statesThisMonth = photos.filter { it.capturedDate >= firstDayOfMonth }
            .map { it.stateCode }
            .distinct()
            .size

        // Average photos per state
        val averagePhotosPerState = if (statesVisited > 0) {
            totalPhotos.toFloat() / statesVisited
        } else 0f

        // Longest journal entry
        val longestJournalLength = journals.maxOfOrNull { it.entryText.length } ?: 0

        TravelStats(
            statesVisited = statesVisited,
            totalPhotos = totalPhotos,
            photosWithJournals = photosWithJournals,
            journalPercentage = journalPercentage,
            mostPhotographedState = mostPhotographedState,
            daysSinceFirstPhoto = daysSinceFirstPhoto,
            photosThisWeek = photosThisWeek,
            photosThisMonth = photosThisMonth,
            statesThisMonth = statesThisMonth,
            averagePhotosPerState = averagePhotosPerState,
            longestJournalLength = longestJournalLength,
            stateBreakdown = statePhotoCounts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TravelStats(
            statesVisited = 0,
            totalPhotos = 0,
            photosWithJournals = 0,
            journalPercentage = 0f,
            mostPhotographedState = null,
            daysSinceFirstPhoto = 0,
            photosThisWeek = 0,
            photosThisMonth = 0,
            statesThisMonth = 0,
            averagePhotosPerState = 0f,
            longestJournalLength = 0,
            stateBreakdown = emptyList()
        )
    )
}
