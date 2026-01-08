package com.example.travellog.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents an item in the bottom navigation bar.
 */
data class BottomNavItem(
    val route: Route,
    val icon: ImageVector,
    val label: String
)

/**
 * List of all bottom navigation items.
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = Route.Map,
        icon = Icons.Default.Explore,
        label = "Map"
    ),
    BottomNavItem(
        route = Route.Gallery,
        icon = Icons.Default.PhotoLibrary,
        label = "Gallery"
    ),
    BottomNavItem(
        route = Route.Stats,
        icon = Icons.Default.BarChart,
        label = "Stats"
    ),
    BottomNavItem(
        route = Route.Settings,
        icon = Icons.Default.Settings,
        label = "Settings"
    )
)
