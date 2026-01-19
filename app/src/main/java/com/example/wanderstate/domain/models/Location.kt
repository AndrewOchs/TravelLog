package com.example.wanderstate.domain.models

data class Location(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)
