package com.example.travellog.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travellog.ui.navigation.BottomNavItem
import com.example.travellog.ui.navigation.Route
import com.example.travellog.ui.navigation.bottomNavItems

/**
 * Bottom navigation bar for the main TravelLog screens.
 *
 * @param currentRoute The currently selected route
 * @param onNavigate Callback when a navigation item is clicked
 * @param modifier Optional modifier for styling
 */
@Composable
fun TravelLogBottomBar(
    currentRoute: Route?,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}
