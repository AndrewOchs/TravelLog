package com.example.travellog.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.travellog.data.models.getStateByCode
import com.example.travellog.ui.components.JournalIndicator
import com.example.travellog.ui.viewmodel.StateDetailViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateDetailScreen(
    stateCode: String,
    onNavigateBack: () -> Unit,
    onNavigateToPhotoDetail: (Long, String, String) -> Unit,
    onNavigateToJournalEntry: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = remember { getStateByCode(stateCode) }
    val viewModel: StateDetailViewModel = hiltViewModel()
    val photos by viewModel.getPhotosWithJournalForState(stateCode).collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state?.name ?: "Unknown State",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (photos.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "No photos yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Start your journey in ${state?.name ?: "this state"} by taking your first photo!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "🌲 Explore nature, capture memories 🏞️",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Photos Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photos) { photoInfo ->
                        PhotoGridItem(
                            photoInfo = photoInfo,
                            onPhotoClick = { photo ->
                                onNavigateToPhotoDetail(photo.id, "state", stateCode)
                            },
                            onLongPress = { photo ->
                                onNavigateToJournalEntry(photo.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoGridItem(
    photoInfo: com.example.travellog.data.models.PhotoWithJournalInfo,
    onPhotoClick: (com.example.travellog.data.entities.PhotoEntity) -> Unit,
    onLongPress: (com.example.travellog.data.entities.PhotoEntity) -> Unit
) {
    val photo = photoInfo.photo
    var showActionSheet by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onPhotoClick(photo) },
                    onLongPress = { showActionSheet = true }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(File(photo.uri)),
                contentDescription = "Photo in ${photo.cityName}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Journal indicator overlay
            JournalIndicator(
                hasJournal = photoInfo.hasJournal,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )

            // City name overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Text(
                    text = photo.cityName.trim(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    // Action bottom sheet for long-press
    if (showActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add/Edit Journal Entry button
                Button(
                    onClick = {
                        showActionSheet = false
                        onLongPress(photo)
                    },
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
                    Text(if (photoInfo.hasJournal) "Edit Journal Entry" else "Add Journal Entry")
                }

                // Delete Photo button
                OutlinedButton(
                    onClick = {
                        showActionSheet = false
                        // TODO: Implement delete functionality
                    },
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

                // Bottom padding
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
