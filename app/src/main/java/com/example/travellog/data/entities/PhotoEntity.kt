package com.example.travellog.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "uri")
    val uri: String,

    @ColumnInfo(name = "state_code")
    val stateCode: String,

    @ColumnInfo(name = "state_name")
    val stateName: String,

    @ColumnInfo(name = "city_name")
    val cityName: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double?,

    @ColumnInfo(name = "longitude")
    val longitude: Double?,

    @ColumnInfo(name = "captured_date")
    val capturedDate: Long,

    @ColumnInfo(name = "added_date")
    val addedDate: Long,

    @ColumnInfo(name = "thumbnail_uri")
    val thumbnailUri: String
)
