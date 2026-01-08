package com.example.travellog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travellog.data.preferences.ThemeMode
import com.example.travellog.ui.viewmodel.SettingsViewModel

/**
 * Settings Screen with organized sections for app configuration.
 *
 * Sections:
 * - Appearance: Theme, dark mode, display preferences
 * - Data & Storage: Storage usage, photo count, cache management
 * - Export & Sharing: Export data, share functionality (placeholder)
 * - About: App version, credits, licenses
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val photoCount by viewModel.photoCount.collectAsState()
    val storageUsed by viewModel.storageUsed.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top app bar
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.primary
            )
        )

        // Settings content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    subtitle = when (currentTheme) {
                        ThemeMode.LIGHT -> "Light mode"
                        ThemeMode.DARK -> "Dark mode"
                        ThemeMode.SYSTEM -> "System default"
                    },
                    onClick = { showThemeDialog = true }
                )
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = "Customize app colors",
                    onClick = { /* TODO: Implement theme customization */ }
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Data & Storage Section
            SettingsSection(title = "Data & Storage") {
                SettingsInfoItem(
                    icon = Icons.Default.Photo,
                    title = "Total Photos",
                    value = photoCount.toString()
                )
                SettingsInfoItem(
                    icon = Icons.Default.Storage,
                    title = "Storage Used",
                    value = storageUsed
                )
                SettingsItem(
                    icon = Icons.Default.DeleteSweep,
                    title = "Clear Cache",
                    subtitle = "Free up space",
                    onClick = { /* TODO: Implement cache clearing */ }
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Export & Sharing Section
            SettingsSection(title = "Export & Sharing") {
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Export Data",
                    subtitle = "Export photos and journals",
                    onClick = { /* TODO: Implement export */ }
                )
                SettingsItem(
                    icon = Icons.Default.CloudUpload,
                    title = "Backup & Sync",
                    subtitle = "Cloud backup options",
                    onClick = { /* TODO: Implement backup */ }
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // About Section
            SettingsSection(title = "About") {
                SettingsInfoItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    value = "1.0.0"
                )
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "Open Source Licenses",
                    subtitle = "View third-party licenses",
                    onClick = { /* TODO: Show licenses */ }
                )
                SettingsItem(
                    icon = Icons.Default.Favorite,
                    title = "Credits",
                    subtitle = "Built with Jetpack Compose",
                    onClick = { /* TODO: Show credits */ }
                )
            }

            // Bottom spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { theme ->
                viewModel.setTheme(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

/**
 * Section header for grouping related settings.
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        content()
    }
}

/**
 * Clickable settings item with icon, title, and subtitle.
 */
@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Non-clickable settings item showing information with icon, title, and value.
 */
@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Dialog for selecting app theme mode.
 */
@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Choose Theme",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Light mode option
                ThemeOption(
                    icon = Icons.Default.LightMode,
                    title = "Light",
                    description = "Always use light theme",
                    isSelected = currentTheme == ThemeMode.LIGHT,
                    onClick = { onThemeSelected(ThemeMode.LIGHT) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dark mode option
                ThemeOption(
                    icon = Icons.Default.DarkMode,
                    title = "Dark",
                    description = "Always use dark theme",
                    isSelected = currentTheme == ThemeMode.DARK,
                    onClick = { onThemeSelected(ThemeMode.DARK) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // System default option
                ThemeOption(
                    icon = Icons.Default.SettingsBrightness,
                    title = "System Default",
                    description = "Follow device theme settings",
                    isSelected = currentTheme == ThemeMode.SYSTEM,
                    onClick = { onThemeSelected(ThemeMode.SYSTEM) }
                )
            }
        }
    }
}

/**
 * Single theme option item in the theme selection dialog.
 */
@Composable
private fun ThemeOption(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
