package com.example.travellog.data.models

import androidx.room.ColumnInfo

/**
 * Data class representing the number of photos per city.
 * Used for aggregated queries with GROUP BY city.
 */
data class CityPhotoCount(
    @ColumnInfo(name = "state_code")
    val stateCode: String,

    @ColumnInfo(name = "city_name")
    val cityName: String,

    @ColumnInfo(name = "photo_count")
    val photoCount: Int
)
