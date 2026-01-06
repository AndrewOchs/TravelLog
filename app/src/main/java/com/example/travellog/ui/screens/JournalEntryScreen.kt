package com.example.travellog.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.travellog.ui.viewmodel.JournalEntryViewModel
import com.example.travellog.ui.viewmodel.JournalSaveState
import java.io.File

/**
 * Screen for creating or editing journal entries for photos.
 * Layout: Photo preview (top 1/3), text input and save button (bottom 2/3).
 *
 * @param photoId ID of the photo to create/edit journal for
 * @param onSave Callback when journal is successfully saved
 * @param onNavigateBack Callback to navigate back
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryScreen(
    photoId: Long,
    onSaveComplete: () -> Unit,  // Callback when save completes - handles navigation
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("DIAGNOSTIC", "JournalEntryScreen COMPOSING - Thread: ${Thread.currentThread().name}")

    val viewModel: JournalEntryViewModel = hiltViewModel()

    // Remember flows to prevent recreating them on every recomposition (which causes infinite loop)
    val photo by remember(photoId) {
        viewModel.getPhoto(photoId)
    }.collectAsState()

    val existingJournal by remember(photoId) {
        viewModel.getJournalForPhoto(photoId)
    }.collectAsState()

    val saveState by viewModel.saveState.collectAsState()

    var entryText by remember { mutableStateOf("") }

    // Initialize text from existing journal entry
    LaunchedEffect(existingJournal) {
        existingJournal?.let { journal ->
            entryText = journal.entryText
        }
    }

    // NO LaunchedEffect - callback pattern handles navigation
    // JournalEntry just saves data, navigation happens in the callback

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (existingJournal != null) "Edit Journal Entry" else "Add Journal Entry")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Photo preview (top 1/3 of screen)
            photo?.let { photoEntity ->
                Image(
                    painter = rememberAsyncImagePainter(File(photoEntity.uri)),
                    contentDescription = "Photo in ${photoEntity.cityName}, ${photoEntity.stateCode}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.33f),
                    contentScale = ContentScale.Crop
                )
            }

            // Text input and save button (bottom 2/3)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Multi-line text input
                OutlinedTextField(
                    value = entryText,
                    onValueChange = { entryText = it },
                    label = { Text("Your journal entry") },
                    placeholder = { Text("Write about this moment...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    minLines = 8,
                    maxLines = 20
                )

                // Show error if save failed
                if (saveState is JournalSaveState.Error) {
                    Text(
                        text = (saveState as JournalSaveState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Save button - simple, just saves and calls callback
                Button(
                    onClick = {
                        Log.d("JournalSave", "Save button clicked - saving journal")
                        // Simple: save with callback that handles navigation
                        viewModel.saveJournal(
                            photoId = photoId,
                            entryText = entryText,
                            onComplete = {
                                Log.d("JournalSave", "Save complete - calling onSaveComplete()")
                                onSaveComplete()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = entryText.isNotBlank() && saveState !is JournalSaveState.Saving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (saveState is JournalSaveState.Saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when {
                            saveState is JournalSaveState.Saving -> "Saving..."
                            existingJournal != null -> "Update Entry"
                            else -> "Save Entry"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}
