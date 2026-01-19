package com.example.wanderstate.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PhotoLibrary
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
import com.example.wanderstate.data.models.PhotoWithJournalInfo
import com.example.wanderstate.ui.components.JournalIndicator
import com.example.wanderstate.ui.viewmodel.GalleryFilter
import com.example.wanderstate.ui.viewmodel.GalleryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onNavigateToPhotoDetail: (Long, String, String) -> Unit,
    onNavigateToJournalEntry: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GalleryViewModel = hiltViewModel()
    val photos by viewModel.allPhotosWithJournalStatus.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()  // Now from ViewModel - survives navigation
    var showFilterMenu by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Section with Filter
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Photo Gallery",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${photos.size} ${if (photos.size == 1) "photo" else "photos"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Filter button
                Box {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("By State")
                                    if (currentFilter == GalleryFilter.BY_STATE) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("✓", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            onClick = {
                                viewModel.setFilter(GalleryFilter.BY_STATE)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("By Date")
                                    if (currentFilter == GalleryFilter.BY_DATE) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("✓", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            onClick = {
                                viewModel.setFilter(GalleryFilter.BY_DATE)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("By City")
                                    if (currentFilter == GalleryFilter.BY_CITY) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("✓", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            onClick = {
                                viewModel.setFilter(GalleryFilter.BY_CITY)
                                showFilterMenu = false
                            }
                        )
                    }
                }
            }
        }

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
                    imageVector = Icons.Default.PhotoLibrary,
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
                    text = "Start capturing your travel memories!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Organize photos based on filter
            when (currentFilter) {
                GalleryFilter.BY_STATE -> {
                    PhotosByState(
                        photos = photos,
                        dateFormat = dateFormat,
                        onNavigateToPhotoDetail = onNavigateToPhotoDetail,
                        onNavigateToJournalEntry = onNavigateToJournalEntry,
                        currentFilter = currentFilter
                    )
                }
                GalleryFilter.BY_DATE -> {
                    PhotosByDate(
                        photos = photos,
                        dateFormat = dateFormat,
                        onNavigateToPhotoDetail = onNavigateToPhotoDetail,
                        onNavigateToJournalEntry = onNavigateToJournalEntry,
                        currentFilter = currentFilter
                    )
                }
                GalleryFilter.BY_CITY -> {
                    PhotosByCity(
                        photos = photos,
                        dateFormat = dateFormat,
                        onNavigateToPhotoDetail = onNavigateToPhotoDetail,
                        onNavigateToJournalEntry = onNavigateToJournalEntry,
                        currentFilter = currentFilter
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotosByState(
    photos: List<PhotoWithJournalInfo>,
    dateFormat: SimpleDateFormat,
    onNavigateToPhotoDetail: (Long, String, String) -> Unit,
    onNavigateToJournalEntry: (Long) -> Unit,
    currentFilter: GalleryFilter
) {
    val photosByState = photos
        .groupBy { it.photo.stateName.trim() }  // Trim to normalize state names
        .toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        photosByState.forEach { (stateName, statePhotos) ->
            item {
                // State header
                Text(
                    text = stateName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(statePhotos.sortedByDescending { it.photo.capturedDate }) { photoInfo ->
                PhotoCard(
                    photoInfo = photoInfo,
                    dateFormat = dateFormat,
                    onPhotoClick = { photo ->
                        onNavigateToPhotoDetail(photo.id, "state", photo.stateCode)
                    },
                    onLongPress = { photo ->
                        onNavigateToJournalEntry(photo.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun PhotosByDate(
    photos: List<PhotoWithJournalInfo>,
    dateFormat: SimpleDateFormat,
    onNavigateToPhotoDetail: (Long, String, String) -> Unit,
    onNavigateToJournalEntry: (Long) -> Unit,
    currentFilter: GalleryFilter
) {
    val sortedPhotos = photos.sortedByDescending { it.photo.capturedDate }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sortedPhotos) { photoInfo ->
            PhotoCard(
                photoInfo = photoInfo,
                dateFormat = dateFormat,
                onPhotoClick = { photo ->
                    onNavigateToPhotoDetail(photo.id, "all", "")
                },
                onLongPress = { photo ->
                    onNavigateToJournalEntry(photo.id)
                }
            )
        }
    }
}

@Composable
private fun PhotosByCity(
    photos: List<PhotoWithJournalInfo>,
    dateFormat: SimpleDateFormat,
    onNavigateToPhotoDetail: (Long, String, String) -> Unit,
    onNavigateToJournalEntry: (Long) -> Unit,
    currentFilter: GalleryFilter
) {
    val photosByStateAndCity = photos
        .groupBy { it.photo.stateName.trim() }  // Trim to normalize state names
        .mapValues { (_, photoInfos) ->
            photoInfos.groupBy { it.photo.cityName.trim() }  // Trim to normalize city names with trailing spaces
        }
        .toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        photosByStateAndCity.forEach { (stateName, citiesMap) ->
            item {
                // State header
                Text(
                    text = stateName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            citiesMap.toSortedMap().forEach { (cityName, cityPhotoInfos) ->
                item {
                    // City subheader
                    Text(
                        text = cityName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                }

                items(cityPhotoInfos.sortedByDescending { it.photo.capturedDate }) { photoInfo ->
                    PhotoCard(
                        photoInfo = photoInfo,
                        dateFormat = dateFormat,
                        onPhotoClick = { photo ->
                            onNavigateToPhotoDetail(photo.id, "city", photo.cityName.trim())
                        },
                        onLongPress = { photo ->
                            onNavigateToJournalEntry(photo.id)
                        },
                        showCity = false
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoCard(
    photoInfo: PhotoWithJournalInfo,
    dateFormat: SimpleDateFormat,
    onPhotoClick: (com.example.wanderstate.data.entities.PhotoEntity) -> Unit,
    onLongPress: (com.example.wanderstate.data.entities.PhotoEntity) -> Unit,
    showCity: Boolean = true
) {
    val photo = photoInfo.photo

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onPhotoClick(photo) },
                    onLongPress = { onPhotoClick(photo) }  // Same as tap - show modern bottom sheet
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            // Photo thumbnail with journal indicator overlay
            Box {
                Image(
                    painter = rememberAsyncImagePainter(File(photo.uri)),
                    contentDescription = "Photo in ${photo.cityName.trim()}, ${photo.stateCode}",
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )

                // Journal indicator overlay
                JournalIndicator(
                    hasJournal = photoInfo.hasJournal,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )
            }

            // Photo info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (showCity) {
                        Text(
                            text = photo.cityName.trim(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = photo.stateCode,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text = photo.stateCode,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = dateFormat.format(Date(photo.capturedDate)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
