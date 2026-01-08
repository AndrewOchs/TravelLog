package com.example.travellog.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    // Main bottom navigation screens
    @Serializable
    data object Map : Route

    @Serializable
    data object Gallery : Route

    @Serializable
    data object Stats : Route

    @Serializable
    data object Settings : Route

    // Secondary screens (accessed from main screens)
    @Serializable
    data class PhotoDetail(
        val photoId: Long,
        val contextType: String = "all",  // "state", "city", "all"
        val contextValue: String = ""     // stateCode or cityName depending on contextType
    ) : Route

    @Serializable
    data class JournalEntry(val photoId: Long) : Route

    @Serializable
    data object Camera : Route

    @Serializable
    data class AddPhotoDetails(val photoUri: String) : Route

    @Serializable
    data class StateDetail(val stateCode: String) : Route

    @Serializable
    data class CityDetail(val stateCode: String, val cityName: String) : Route
}
