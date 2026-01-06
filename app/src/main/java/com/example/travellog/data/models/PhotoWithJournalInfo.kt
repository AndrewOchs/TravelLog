package com.example.travellog.data.models

import com.example.travellog.data.entities.PhotoEntity

/**
 * Data class combining photo entity with journal existence status.
 * Used to efficiently display photos with journal indicators in UI without N+1 queries.
 *
 * @property photo The photo entity
 * @property hasJournal True if this photo has an associated journal entry
 */
data class PhotoWithJournalInfo(
    val photo: PhotoEntity,
    val hasJournal: Boolean
)
