package com.example.travellog.ui.screens

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.travellog.data.models.allUsStates
import com.example.travellog.ui.components.InteractiveUsMap
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    states: List<com.example.travellog.data.models.UsState>,
    onStateClick: (String) -> Unit = {},
    onPhotoSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isLoading by remember { mutableStateOf(true) }
    var showPhotoSourceSheet by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera permission state
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Calculate stats
    val visitedStates = remember(states) { states.count { it.photoCount > 0 } }
    val totalPhotos = remember(states) { states.sumOf { it.photoCount } }

    // Helper function to create temp file URI for camera
    fun createTempImageUri(): Uri {
        val tempFile = File.createTempFile(
            "camera_photo_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        ).apply {
            deleteOnExit()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    // Photo picker for gallery selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onPhotoSelected(it.toString()) }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            onPhotoSelected(tempCameraUri.toString())
            tempCameraUri = null
        }
    }

    // Simulate loading (in real app, would load from database)
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        isLoading = false
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Title Section - compact in landscape
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isLandscape) 8.dp else 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Travel Map",
                        style = if (isLandscape)
                            MaterialTheme.typography.titleLarge
                        else
                            MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Hide subtitle in landscape to save space
                    if (!isLandscape) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$visitedStates states visited • $totalPhotos total photos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (isLoading) {
                // Loading State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading map...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Map Content with built-in legend and state selection
                InteractiveUsMap(
                    states = states,
                    onStateClick = { state ->
                        onStateClick(state.code)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Camera FAB - top-right (portrait) or top-left (landscape)
        FloatingActionButton(
            onClick = { showPhotoSourceSheet = true },
            modifier = Modifier
                .align(if (isLandscape) Alignment.TopStart else Alignment.TopEnd)
                .padding(
                    top = if (isLandscape) 56.dp else 88.dp,
                    start = if (isLandscape) 16.dp else 0.dp,
                    end = if (isLandscape) 0.dp else 16.dp
                ),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(
                imageVector = Icons.Filled.AddAPhoto,
                contentDescription = "Take Photo"
            )
        }
    }

    // Photo source selection bottom sheet
    if (showPhotoSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Add Photo",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Take Photo option
                Surface(
                    onClick = {
                        showPhotoSourceSheet = false
                        if (cameraPermissionState.status.isGranted) {
                            tempCameraUri = createTempImageUri()
                            tempCameraUri?.let { cameraLauncher.launch(it) }
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Take Photo",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Capture a new photo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Choose from Gallery option
                Surface(
                    onClick = {
                        showPhotoSourceSheet = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Choose from Gallery",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Select an existing photo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
