package com.example.travellog.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.travellog.ui.viewmodel.PhotoDetailViewModel
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
    val currentPhoto by viewModel.currentPhoto.collectAsState()
    val currentJournal by viewModel.currentJournal.collectAsState()
    val photoList by viewModel.photoList.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var isNavigatingToJournal by rememberSaveable { mutableStateOf(false) }  // Guard survives recomposition
    val pagerState = rememberPagerState(pageCount = { photoList.size })

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
            val index = photoList.indexOfFirst { it.id == photoId }
            Log.d("PhotoDetailScreen", "Photo list changed. Size=${photoList.size}, Looking for photoId=$photoId, found at index=$index, currentPage=${pagerState.currentPage}")
            if (index >= 0 && index != pagerState.currentPage) {
                Log.d("PhotoDetailScreen", "Scrolling pager to page $index")
                pagerState.scrollToPage(index)
            }
        } else {
            Log.d("PhotoDetailScreen", "Photo list is EMPTY")
        }
    }

    // Update current photo when pager page changes
    LaunchedEffect(pagerState.currentPage) {
        if (photoList.isNotEmpty() && pagerState.currentPage < photoList.size) {
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
    }

    // Bottom sheet with photo info and actions
    if (showBottomSheet && currentPhoto != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            modifier = Modifier.fillMaxHeight(0.6f)
        ) {
            PhotoInfoBottomSheet(
                photo = currentPhoto!!,
                journal = currentJournal,
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
                onDeletePhoto = {
                    Log.d("PhotoDetail", "Delete photo clicked")
                    viewModel.deleteCurrentPhoto()
                    // Don't update showBottomSheet - navigation will dismiss it automatically
                    onNavigateBack()
                }
            )
        }
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
    onEditJournal: () -> Unit,
    onDeletePhoto: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location info
        item {
            Column {
                Text(
                    text = "${photo.cityName.trim()}, ${photo.stateCode}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
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

        // Edit journal button
        item {
            Button(
                onClick = onEditJournal,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (journal != null) "Edit Journal Entry" else "Add Journal Entry")
            }
        }

        // Delete photo button
        item {
            OutlinedButton(
                onClick = onDeletePhoto,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Delete Photo")
            }
        }

        // Bottom padding for safe area
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
