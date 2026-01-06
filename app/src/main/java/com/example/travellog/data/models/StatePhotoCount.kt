package com.example.travellog.data.models

import androidx.room.ColumnInfo

/**
 * Data class representing the number of photos per state.
 * Used for aggregated queries with GROUP BY state.
 */
data class StatePhotoCount(
    @ColumnInfo(name = "state_code")
    val stateCode: String,

    @ColumnInfo(name = "state_name")
    val stateName: String,

    @ColumnInfo(name = "photo_count")
    val photoCount: Int
)
