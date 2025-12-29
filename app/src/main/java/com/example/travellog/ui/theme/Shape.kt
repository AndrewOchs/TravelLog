package com.example.travellog.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val TravelLogShapes = Shapes(
    // Small components (buttons, chips)
    small = RoundedCornerShape(8.dp),

    // Medium components (cards, text fields)
    medium = RoundedCornerShape(16.dp),

    // Large components (bottom sheets, dialogs)
    large = RoundedCornerShape(24.dp)
)
