package com.example.travellog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToTripList: () -> Unit,
    onNavigateToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to TravelLog",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateToTripList,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Trips")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToCamera,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Photo")
        }
    }
}
