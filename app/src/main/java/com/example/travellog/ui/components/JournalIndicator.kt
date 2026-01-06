package com.example.travellog.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Reusable journal indicator overlay component.
 * Shows a small circular icon when a photo has a journal entry.
 * Designed to be placed as an overlay in a Box layout.
 *
 * @param hasJournal Whether the photo has a journal entry
 * @param modifier Modifier for positioning (typically align to bottom-end with padding)
 */
@Composable
fun JournalIndicator(
    hasJournal: Boolean,
    modifier: Modifier = Modifier
) {
    if (hasJournal) {
        Surface(
            modifier = modifier.size(24.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Has journal entry",
                tint = Color.White,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize()
            )
        }
    }
}
