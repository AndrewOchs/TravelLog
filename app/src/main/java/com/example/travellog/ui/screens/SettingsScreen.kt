package com.example.travellog.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.travellog.data.preferences.ThemeMode
import com.example.travellog.ui.viewmodel.ExportState
import com.example.travellog.ui.viewmodel.ExportViewModel
import com.example.travellog.ui.viewmodel.SettingsViewModel

private const val TAG = "SettingsScreen"

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
    var showExportDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var cacheCleared by remember { mutableStateOf(false) }

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
                    subtitle = if (cacheCleared) "Cache cleared!" else "Free up space",
                    onClick = { showClearCacheDialog = true }
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
                    title = "Export Photo",
                    subtitle = "Share photo with journal overlay",
                    onClick = { showExportDialog = true }
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
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    onClick = { showPrivacyPolicyDialog = true }
                )
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "Open Source Licenses",
                    subtitle = "View third-party licenses",
                    onClick = { showLicensesDialog = true }
                )

                val context = LocalContext.current
                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Send Feedback",
                    subtitle = "Share your thoughts with us",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("andrewochs5@gmail.com"))
                            putExtra(Intent.EXTRA_SUBJECT, "TravelLog Feedback")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to open email app", e)
                        }
                    }
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

    // Export Photo Dialog
    if (showExportDialog) {
        ExportPhotoDialog(
            onDismiss = { showExportDialog = false }
        )
    }

    // Clear Cache Confirmation Dialog
    if (showClearCacheDialog) {
        ClearCacheDialog(
            viewModel = viewModel,
            onDismiss = { showClearCacheDialog = false },
            onCacheCleared = {
                cacheCleared = true
                showClearCacheDialog = false
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }

    // Open Source Licenses Dialog
    if (showLicensesDialog) {
        OpenSourceLicensesDialog(
            onDismiss = { showLicensesDialog = false }
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

/**
 * Dialog for selecting and exporting a photo with journal overlay.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportPhotoDialog(
    onDismiss: () -> Unit
) {
    val exportViewModel: ExportViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val photos by settingsViewModel.allPhotos.collectAsState()
    val exportState by exportViewModel.exportState.collectAsState()
    val context = LocalContext.current

    var selectedPhotoId by remember { mutableStateOf<Long?>(null) }

    // Handle export success - share the file
    LaunchedEffect(exportState) {
        if (exportState is ExportState.Success) {
            Log.d(TAG, "Export success! Creating share intent")
            val uri = (exportState as ExportState.Success).fileUri
            Log.d(TAG, "Share URI: $uri")

            // Create share intent with proper permissions
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                // CRITICAL: Grant read permission so receiving apps can access the file
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Verify intent configuration
            Log.d(TAG, "Share Intent verification:")
            Log.d(TAG, "  Type: ${shareIntent.type}")
            Log.d(TAG, "  URI: ${shareIntent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)}")
            Log.d(TAG, "  Flags: ${shareIntent.flags} (includes FLAG_GRANT_READ_URI_PERMISSION: ${shareIntent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0})")

            // Create chooser with proper flags
            val chooserIntent = Intent.createChooser(shareIntent, "Share Photo").apply {
                // Add NEW_TASK flag when starting from non-Activity context
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            Log.d(TAG, "Launching share chooser with permissions")
            try {
                context.startActivity(chooserIntent)
                Log.d(TAG, "Share chooser launched successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch share chooser", e)
            }

            exportViewModel.resetExportState()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Text(
                text = "Export Photo with Journal",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Select a photo to export with location and journal overlay",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (exportState) {
                is ExportState.Loading -> {
                    // Loading state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Generating shareable image...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is ExportState.Error -> {
                    // Error state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = (exportState as ExportState.Error).message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { exportViewModel.resetExportState() }) {
                                Text("Try Again")
                            }
                        }
                    }
                }

                else -> {
                    // Photo selection list
                    if (photos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No photos available to export",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(photos) { photo ->
                                PhotoExportItem(
                                    photo = photo,
                                    isSelected = selectedPhotoId == photo.id,
                                    onClick = { selectedPhotoId = photo.id }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // Export button
                        Button(
                            onClick = {
                                selectedPhotoId?.let { photoId ->
                                    exportViewModel.exportPhotoWithJournal(photoId)
                                }
                            },
                            enabled = selectedPhotoId != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export & Share")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual photo item for export selection.
 */
@Composable
private fun PhotoExportItem(
    photo: com.example.travellog.data.entities.PhotoEntity,
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
            MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail
            Image(
                painter = rememberAsyncImagePainter(photo.uri),
                contentDescription = "${photo.stateName} photo",
                modifier = Modifier
                    .size(60.dp),
                contentScale = ContentScale.Crop
            )

            // Photo info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${photo.stateName} - ${photo.cityName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US)
                        .format(java.util.Date(photo.capturedDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Dialog for clearing app cache with confirmation.
 */
@Composable
private fun ClearCacheDialog(
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit,
    onCacheCleared: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cacheSize = remember { viewModel.getCacheSize() }
    var isClearing by remember { mutableStateOf(false) }
    var clearError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isClearing) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Clear Cache?",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "This will delete all temporary files, including exported images.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Current cache size: $cacheSize",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (clearError != null) {
                    Text(
                        text = clearError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isClearing = true
                    clearError = null
                    scope.launch {
                        val success = viewModel.clearCache()
                        isClearing = false
                        if (success) {
                            onCacheCleared()
                        } else {
                            clearError = "Failed to clear cache"
                        }
                    }
                },
                enabled = !isClearing
            ) {
                if (isClearing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Clear Cache")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isClearing
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog showing the app's privacy policy.
 */
@Composable
private fun PrivacyPolicyDialog(
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
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Scrollable privacy policy content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Last Updated: January 2026",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    PrivacyPolicySection(
                        title = "Data Collection",
                        content = "TravelLog stores all data locally on your device. We do not collect, transmit, or share any of your personal information, photos, journal entries, or location data with third parties."
                    )

                    PrivacyPolicySection(
                        title = "Photos and Location",
                        content = "Photos you capture are stored locally in your device's app storage. Location data is extracted from photo EXIF metadata and stored in a local database. This data never leaves your device unless you explicitly choose to export and share it."
                    )

                    PrivacyPolicySection(
                        title = "Permissions",
                        content = "TravelLog requires camera and storage permissions to capture and store photos. Location permission is not required as location data is read from photo metadata."
                    )

                    PrivacyPolicySection(
                        title = "Data Export",
                        content = "When you use the export feature, shareable images are temporarily created in the app cache and shared via Android's share sheet. You control where these files are shared."
                    )

                    PrivacyPolicySection(
                        title = "Data Deletion",
                        content = "You can delete individual photos and journal entries at any time within the app. Uninstalling the app will permanently delete all local data."
                    )

                    PrivacyPolicySection(
                        title = "Third-Party Services",
                        content = "TravelLog does not integrate with any third-party analytics, advertising, or tracking services. Your data remains private and under your control."
                    )

                    PrivacyPolicySection(
                        title = "Changes to This Policy",
                        content = "We may update this privacy policy from time to time. Any changes will be reflected in the app with an updated \"Last Updated\" date."
                    )

                    PrivacyPolicySection(
                        title = "Contact",
                        content = "If you have questions about this privacy policy, please contact us at feedback@travellog.app"
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

/**
 * Privacy policy section with title and content.
 */
@Composable
private fun PrivacyPolicySection(
    title: String,
    content: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Dialog showing open source licenses for third-party libraries.
 */
@Composable
private fun OpenSourceLicensesDialog(
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
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Open Source Licenses",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Scrollable licenses list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "TravelLog is built with the following open source libraries:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LicenseItem(
                        libraryName = "Jetpack Compose",
                        license = "Apache License 2.0",
                        description = "Modern toolkit for building native Android UI"
                    )

                    LicenseItem(
                        libraryName = "Kotlin",
                        license = "Apache License 2.0",
                        description = "Modern programming language for Android development"
                    )

                    LicenseItem(
                        libraryName = "Room Database",
                        license = "Apache License 2.0",
                        description = "Persistence library for local data storage"
                    )

                    LicenseItem(
                        libraryName = "Hilt (Dagger)",
                        license = "Apache License 2.0",
                        description = "Dependency injection library"
                    )

                    LicenseItem(
                        libraryName = "Kotlin Coroutines",
                        license = "Apache License 2.0",
                        description = "Library for asynchronous programming"
                    )

                    LicenseItem(
                        libraryName = "Coil",
                        license = "Apache License 2.0",
                        description = "Image loading library for Android"
                    )

                    LicenseItem(
                        libraryName = "ExifInterface",
                        license = "Apache License 2.0",
                        description = "Library for reading/writing EXIF data in images"
                    )

                    LicenseItem(
                        libraryName = "DataStore",
                        license = "Apache License 2.0",
                        description = "Data storage solution for key-value pairs"
                    )

                    LicenseItem(
                        libraryName = "Material Design 3",
                        license = "Apache License 2.0",
                        description = "Material Design components for Android"
                    )

                    Text(
                        text = "All libraries are licensed under Apache License 2.0, which allows for commercial and private use, modification, distribution, and patent use.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

/**
 * Individual license item showing library name, license type, and description.
 */
@Composable
private fun LicenseItem(
    libraryName: String,
    license: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = libraryName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = license,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
