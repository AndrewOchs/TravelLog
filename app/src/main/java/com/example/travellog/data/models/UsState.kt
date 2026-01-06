package com.example.travellog.data.models

/**
 * Represents a US state with its position on the map and photo count.
 * Coordinates are normalized to 0-1 range for easy scaling.
 */
data class UsState(
    val code: String,
    val name: String,
    val centerX: Float,
    val centerY: Float,
    val size: StateSize = StateSize.MEDIUM,
    val photoCount: Int = 0
)

/**
 * Relative size categories for states to make map more realistic.
 */
enum class StateSize(val scale: Float) {
    TINY(0.6f),      // RI, DE, CT, DC
    SMALL(0.8f),     // NH, VT, MA, NJ, MD, HI
    MEDIUM(1.0f),    // Most states
    LARGE(1.3f),     // NY, FL, IL, PA, OH, GA, NC, MI, VA, WI
    XLARGE(1.6f)     // TX, CA, MT, AZ, NV, CO, NM, WY, OR, UT, MN, AK
}

/**
 * All 50 US states with approximate center coordinates and relative sizes.
 * Coordinates are normalized (0-1 range) for canvas scaling.
 * Alaska and Hawaii positioned separately in bottom left insets.
 */
val allUsStates = listOf(
    // West Coast
    UsState("WA", "Washington", 0.10f, 0.12f, StateSize.LARGE),
    UsState("OR", "Oregon", 0.08f, 0.22f, StateSize.XLARGE),
    UsState("CA", "California", 0.06f, 0.42f, StateSize.XLARGE),

    // Mountain West
    UsState("MT", "Montana", 0.21f, 0.14f, StateSize.XLARGE),
    UsState("ID", "Idaho", 0.16f, 0.23f, StateSize.MEDIUM),
    UsState("WY", "Wyoming", 0.24f, 0.28f, StateSize.XLARGE),
    UsState("NV", "Nevada", 0.12f, 0.38f, StateSize.XLARGE),
    UsState("UT", "Utah", 0.19f, 0.40f, StateSize.XLARGE),
    UsState("CO", "Colorado", 0.27f, 0.43f, StateSize.XLARGE),
    UsState("AZ", "Arizona", 0.17f, 0.55f, StateSize.XLARGE),
    UsState("NM", "New Mexico", 0.25f, 0.58f, StateSize.XLARGE),

    // Northern Plains
    UsState("ND", "North Dakota", 0.37f, 0.18f, StateSize.MEDIUM),
    UsState("SD", "South Dakota", 0.37f, 0.28f, StateSize.MEDIUM),
    UsState("NE", "Nebraska", 0.38f, 0.40f, StateSize.MEDIUM),
    UsState("KS", "Kansas", 0.39f, 0.48f, StateSize.MEDIUM),

    // Central
    UsState("MN", "Minnesota", 0.47f, 0.20f, StateSize.XLARGE),
    UsState("IA", "Iowa", 0.47f, 0.33f, StateSize.MEDIUM),
    UsState("MO", "Missouri", 0.49f, 0.45f, StateSize.MEDIUM),
    UsState("WI", "Wisconsin", 0.54f, 0.26f, StateSize.LARGE),
    UsState("IL", "Illinois", 0.57f, 0.40f, StateSize.LARGE),
    UsState("IN", "Indiana", 0.62f, 0.40f, StateSize.MEDIUM),
    UsState("MI", "Michigan", 0.62f, 0.28f, StateSize.LARGE),
    UsState("OH", "Ohio", 0.68f, 0.40f, StateSize.LARGE),

    // South Central
    UsState("OK", "Oklahoma", 0.39f, 0.56f, StateSize.MEDIUM),
    UsState("TX", "Texas", 0.33f, 0.68f, StateSize.XLARGE),
    UsState("AR", "Arkansas", 0.48f, 0.56f, StateSize.MEDIUM),
    UsState("LA", "Louisiana", 0.50f, 0.70f, StateSize.MEDIUM),

    // Upper South
    UsState("KY", "Kentucky", 0.66f, 0.48f, StateSize.MEDIUM),
    UsState("TN", "Tennessee", 0.64f, 0.54f, StateSize.MEDIUM),
    UsState("WV", "West Virginia", 0.71f, 0.46f, StateSize.MEDIUM),
    UsState("VA", "Virginia", 0.76f, 0.48f, StateSize.LARGE),
    UsState("NC", "North Carolina", 0.77f, 0.56f, StateSize.LARGE),
    UsState("SC", "South Carolina", 0.75f, 0.62f, StateSize.MEDIUM),

    // Deep South
    UsState("MS", "Mississippi", 0.54f, 0.63f, StateSize.MEDIUM),
    UsState("AL", "Alabama", 0.61f, 0.63f, StateSize.MEDIUM),
    UsState("GA", "Georgia", 0.69f, 0.63f, StateSize.LARGE),
    UsState("FL", "Florida", 0.74f, 0.74f, StateSize.LARGE),

    // Mid-Atlantic
    UsState("MD", "Maryland", 0.78f, 0.46f, StateSize.SMALL),
    UsState("DE", "Delaware", 0.81f, 0.46f, StateSize.TINY),
    UsState("NJ", "New Jersey", 0.82f, 0.41f, StateSize.SMALL),
    UsState("PA", "Pennsylvania", 0.76f, 0.40f, StateSize.LARGE),
    UsState("NY", "New York", 0.79f, 0.33f, StateSize.LARGE),

    // New England
    UsState("CT", "Connecticut", 0.84f, 0.37f, StateSize.TINY),
    UsState("RI", "Rhode Island", 0.86f, 0.38f, StateSize.TINY),
    UsState("MA", "Massachusetts", 0.86f, 0.34f, StateSize.SMALL),
    UsState("VT", "Vermont", 0.83f, 0.27f, StateSize.SMALL),
    UsState("NH", "New Hampshire", 0.86f, 0.27f, StateSize.SMALL),
    UsState("ME", "Maine", 0.89f, 0.20f, StateSize.MEDIUM),

    // Alaska & Hawaii (positioned separately in bottom left insets)
    UsState("AK", "Alaska", 0.08f, 0.88f, StateSize.XLARGE),
    UsState("HI", "Hawaii", 0.20f, 0.92f, StateSize.SMALL)
)

/**
 * Get a state by its code.
 */
fun getStateByCode(code: String): UsState? {
    return allUsStates.find { it.code == code }
}

/**
 * Update photo counts for states based on database data.
 */
fun updateStatePhotoCounts(photoCounts: Map<String, Int>): List<UsState> {
    return allUsStates.map { state ->
        state.copy(photoCount = photoCounts[state.code] ?: 0)
    }
}
