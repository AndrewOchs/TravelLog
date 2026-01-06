package com.example.travellog.data.models

import androidx.room.ColumnInfo

/**
 * Data class representing basic state information.
 * Used for queries that only need state identification without full photo details.
 */
data class StateInfo(
    @ColumnInfo(name = "state_code")
    val stateCode: String,

    @ColumnInfo(name = "state_name")
    val stateName: String
)
