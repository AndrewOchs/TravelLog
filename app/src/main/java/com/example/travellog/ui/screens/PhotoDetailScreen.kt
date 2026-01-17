package com.example.travellog.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.travellog.ui.viewmodel.PhotoDetailViewModel
import com.example.travellog.ui.viewmodel.ExportViewModel
import com.example.travellog.ui.viewmodel.ExportState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Full-screen photo detail screen with swipe navigation and bottom sheet.
 * Supports pinch-to-zoom, double-tap zoom, and context-aware photo swiping.
 *
 * @param photoId Initial photo ID to display
 * @param contextType Context type: "state", "city", or "all"
 * @param contextValue Context value (state code or city name)
 * @param onNavigateBack Callback to navigate back
 * @param onNavigateToJournalEntry Callback to navigate to journal entry screen
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PhotoDetailScreen(
    photoId: Long,
    contextType: String,
    contextValue: String,
    onNavigateBack: () -> Unit,
    onNavigateToJournalEntry: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("DIAGNOSTIC", "PhotoDetailScreen COMPOSING - Thread: ${Thread.currentThread().name}")

    val viewModel: PhotoDetailViewModel = hiltViewModel()
    val exportViewModel: ExportViewModel = hiltViewModel()
    val context = LocalContext.current

    val currentPhoto by viewModel.currentPhoto.collectAsState()
    val currentJournal by viewModel.currentJournal.collectAsState()
    val photoList by viewModel.photoList.collectAsState()
    val exportState by exportViewModel.exportState.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var isNavigatingToJournal by rememberSaveable { mutableStateOf(false) }  // Guard survives recomposition

    // Save current page index across configuration changes (like rotation)
    var savedPageIndex by rememberSaveable { mutableStateOf(-1) }
    val pagerState = rememberPagerState(
        pageCount = { photoList.size }
    )

    // Export preview state
    var showExportPreview by remember { mutableStateOf(false) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scope = rememberCoroutineScope()

    // Reset navigation guard when returning to this screen or photo changes
    LaunchedEffect(currentPhoto?.id) {
        Log.d("PhotoDetailScreen", "Photo changed or screen resumed (photoId=${currentPhoto?.id}) - resetting navigation guard")
        isNavigatingToJournal = false
    }

    // Initialize ViewModel with context
    LaunchedEffect(photoId, contextType, contextValue) {
        Log.d("PhotoDetailScreen", "Initializing with photoId=$photoId, contextType=$contextType, contextValue=$contextValue")
        viewModel.initialize(photoId, contextType, contextValue)
    }

    // Sync pager with current photo when photo list loads
    LaunchedEffect(photoList, photoId) {
        if (photoList.isNotEmpty()) {
            // After rotation, restore to saved page if available
            val targetIndex = if (savedPageIndex >= 0 && savedPageIndex < photoList.size) {
                Log.d("PhotoDetailScreen", "Restoring saved page index: $savedPageIndex after rotation")
                savedPageIndex
            } else {
                // Normal case: find the photo by ID
                val index = photoList.indexOfFirst { it.id == photoId }
                Log.d("PhotoDetailScreen", "Photo list changed. Size=${photoList.size}, Looking for photoId=$photoId, found at index=$index, currentPage=${pagerState.currentPage}")
                index
            }

            if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
                Log.d("PhotoDetailScreen", "Scrolling pager to page $targetIndex")
                pagerState.scrollToPage(targetIndex)
            }
        } else {
            Log.d("PhotoDetailScreen", "Photo list is EMPTY")
        }
    }

    // Update current photo when pager page changes and save page index
    LaunchedEffect(pagerState.currentPage) {
        if (photoList.isNotEmpty() && pagerState.currentPage < photoList.size) {
            // Save current page for rotation persistence
            savedPageIndex = pagerState.currentPage

            val newPhotoId = photoList[pagerState.currentPage].id
            Log.d("PhotoDetailScreen", "Pager page changed to ${pagerState.currentPage}, newPhotoId=$newPhotoId, currentPhotoId=${viewModel.currentPhotoId.value}")
            if (newPhotoId != viewModel.currentPhotoId.value) {
                viewModel.updateCurrentPhoto(newPhotoId)
            }
        }
    }

    // Handle deleted photo - navigate back if current photo is no longer in list
    LaunchedEffect(photoList, viewModel.currentPhotoId.value) {
        if (photoList.isNotEmpty() && !photoList.any { it.id == viewModel.currentPhotoId.value }) {
            Log.e("PhotoDetail", "CRITICAL: Current photo (${viewModel.currentPhotoId.value}) NOT FOUND in photo list! Navigating back. PhotoList IDs: ${photoList.map { it.id }}")
            onNavigateBack()
        } else {
            Log.d("PhotoDetail", "Delete check: Photo ${viewModel.currentPhotoId.value} found in list of ${photoList.size} photos")
        }
    }

    // Handle export state - show share dialog when export succeeds
    LaunchedEffect(exportState) {
        if (exportState is ExportState.Success) {
            val fileUri = (exportState as ExportState.Success).fileUri
            Log.d("PhotoDetail", "Export succeeded, launching share intent for URI: $fileUri")

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Photo"))
            exportViewModel.resetExportState()
        } else if (exportState is ExportState.Error) {
            Log.e("PhotoDetail", "Export failed: ${(exportState as ExportState.Error).message}")
            // Error is handled in the PhotoInfoBottomSheet
        }
    }

    // Log pager state for debugging
    LaunchedEffect(pagerState.currentPage, photoList.size) {
        Log.d("PhotoDetailSwipe", "═════════════════════════════════════")
        Log.d("PhotoDetailSwipe", "PAGER STATE:")
        Log.d("PhotoDetailSwipe", "  Current page: ${pagerState.currentPage}")
        Log.d("PhotoDetailSwipe", "  Total pages: ${photoList.size}")
        Log.d("PhotoDetailSwipe", "  Can scroll forward: ${pagerState.canScrollForward}")
        Log.d("PhotoDetailSwipe", "  Can scroll backward: ${pagerState.canScrollBackward}")
        Log.d("PhotoDetailSwipe", "═════════════════════════════════════")
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Horizontal pager for swipe navigation
        if (photoList.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true  // Explicitly enable user scrolling
            ) { page ->
                val photo = photoList[page]

                Log.d("PhotoDetailSwipe", "Rendering page $page with photo ${photo.id}")

                // Zoomable photo view
                ZoomablePhotoView(
                    photoUri = photo.uri,
                    onTap = { showBottomSheet = !showBottomSheet }
                )
            }
        }

        // Top bar with back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(8.dp)
            )
        }

        // Page indicator
        if (photoList.size > 1) {
            Text(
                text = "${pagerState.currentPage + 1} / ${photoList.size}",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Three-dot menu FAB for better discoverability
        FloatingActionButton(
            onClick = { showBottomSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Photo options",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    // Bottom sheet with photo info and actions
    if (showBottomSheet && currentPhoto != null) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        )

        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth()
        ) {
            PhotoInfoBottomSheet(
                photo = currentPhoto!!,
                journal = currentJournal,
                exportState = exportState,
                contextType = contextType,
                onEditJournal = {
                    // Guard against navigation loop - only navigate once
                    if (!isNavigatingToJournal) {
                        Log.d("PhotoDetail", "Edit journal clicked - setting navigation guard and navigating")
                        isNavigatingToJournal = true
                        onNavigateToJournalEntry(currentPhoto!!.id)
                    } else {
                        Log.d("PhotoDetail", "Edit journal clicked but ALREADY navigating - ignoring")
                    }
                },
                onExportPhoto = {
                    Log.d("PhotoDetail", "Export photo clicked for ID: ${currentPhoto!!.id}")
                    // Generate preview first
                    scope.launch {
                        val bitmap = exportViewModel.generateExportPreview(currentPhoto!!.id)
                        if (bitmap != null) {
                            previewBitmap = bitmap
                            showExportPreview = true
                        } else {
                            Log.e("PhotoDetail", "Failed to generate preview")
                        }
                    }
                },
                onDeletePhoto = {
                    Log.d("PhotoDetail", "Delete photo clicked")
                    viewModel.deleteCurrentPhoto()
                    // Don't update showBottomSheet - navigation will dismiss it automatically
                    onNavigateBack()
                },
                onUpdateCity = { photoId, newCity ->
                    Log.d("PhotoDetail", "Update city clicked for photo ID: $photoId, new city: $newCity")
                    viewModel.updatePhotoCity(photoId, newCity)

                    // If viewing photos filtered by city, navigate back since grouping changed
                    if (contextType == "city") {
                        Log.d("PhotoDetail", "City changed while filtered by city - navigating back to gallery")
                        showBottomSheet = false
                        onNavigateBack()
                    }
                }
            )
        }
    }

    // Show export preview dialog
    if (showExportPreview && previewBitmap != null) {
        ExportPreviewDialog(
            bitmap = previewBitmap!!,
            onDismiss = {
                showExportPreview = false
                previewBitmap?.recycle()
                previewBitmap = null
            },
            onConfirm = { bitmap ->
                Log.d("PhotoDetail", "Preview confirmed, saving and sharing")
                exportViewModel.saveAndShareBitmap(bitmap)
                showExportPreview = false
                // Don't recycle bitmap yet - it's being saved
                previewBitmap = null
            }
        )
    }
}

/**
 * Photo view with double-tap zoom ONLY.
 * Pinch-to-zoom removed to avoid consuming horizontal swipe gestures.
 * This allows HorizontalPager to work correctly.
 */
@Composable
private fun ZoomablePhotoView(
    photoUri: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    val scope = rememberCoroutineScope()

    Log.d("PhotoDetailSwipe", "ZoomablePhotoView: scale=$scale")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            // ONLY tap gestures - no transform gestures that would consume swipes
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        Log.d("PhotoDetailSwipe", "Single tap detected")
                        onTap()
                    },
                    onDoubleTap = {
                        val targetScale = if (scale > 1.5f) 1f else 2f
                        Log.d("PhotoDetailSwipe", "Double tap: scale $scale -> $targetScale")
                        scope.launch {
                            val scaleAnim = Animatable(scale)
                            scaleAnim.animateTo(targetScale, spring())
                            scale = scaleAnim.value
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(File(photoUri)),
            contentDescription = "Full photo",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                ),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Bottom sheet content showing photo information and actions.
 */
@Composable
private fun PhotoInfoBottomSheet(
    photo: com.example.travellog.data.entities.PhotoEntity,
    journal: com.example.travellog.data.entities.JournalEntryEntity?,
    exportState: ExportState,
    contextType: String,
    onEditJournal: () -> Unit,
    onExportPhoto: () -> Unit,
    onDeletePhoto: () -> Unit,
    onUpdateCity: (Long, String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }
    val isExporting = exportState is ExportState.Loading
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditLocationDialog by remember { mutableStateOf(false) }
    var editedCity by remember(photo.id) { mutableStateOf(photo.cityName.trim()) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location info
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${photo.cityName.trim()}, ${photo.stateCode}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showEditLocationDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit location",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = photo.stateName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Date
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(photo.capturedDate)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Journal entry (if exists)
        if (journal != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Journal Entry",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = journal.entryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Last updated: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(journal.updatedDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Action buttons row (icon-only with bubble styling)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Edit Journal button
                IconButton(
                    onClick = onEditJournal,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                        .size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = if (journal != null) "Edit Journal Entry" else "Add Journal Entry",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Share Photo button
                IconButton(
                    onClick = onExportPhoto,
                    enabled = !isExporting,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        )
                        .size(56.dp)
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Photo with Journal",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = CircleShape
                        )
                        .size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Photo",
                        tint = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Export error message
        if (exportState is ExportState.Error) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = exportState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Bottom padding for safe area
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Edit location dialog
    if (showEditLocationDialog) {
        AlertDialog(
            onDismissRequest = { showEditLocationDialog = false },
            title = { Text("Edit Location") },
            text = {
                Column {
                    Text(
                        text = "Update the city/town name:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = editedCity,
                        onValueChange = { editedCity = it },
                        label = { Text("City/Town") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "State: ${photo.stateName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Warning if in city-filtered context
                    if (contextType == "city") {
                        Text(
                            text = "Note: Changing the city will return you to the gallery.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateCity(photo.id, editedCity.trim())
                        showEditLocationDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditLocationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Photo?") },
            text = {
                Text("Are you sure you want to delete this photo? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeletePhoto()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Preview dialog for export - shows the Polaroid before sharing.
 */
@Composable
fun ExportPreviewDialog(
    bitmap: Bitmap,
    onDismiss: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f)),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Preview Export",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Image preview (scrollable if needed)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Export preview",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(bitmap) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Share")
                    }
                }
            }
        }
    }
}
