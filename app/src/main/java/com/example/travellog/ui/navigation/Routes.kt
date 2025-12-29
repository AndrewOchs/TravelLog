package com.example.travellog.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data object TripList : Route

    @Serializable
    data class TripDetail(val tripId: String) : Route

    @Serializable
    data object Camera : Route

    @Serializable
    data object Settings : Route
}
