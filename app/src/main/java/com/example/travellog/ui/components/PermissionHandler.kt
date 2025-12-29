package com.example.travellog.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.runtime.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberCameraPermissions() = rememberMultiplePermissionsState(
    permissions = listOf(Manifest.permission.CAMERA)
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationPermissions() = rememberMultiplePermissionsState(
    permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberStoragePermissions() = rememberMultiplePermissionsState(
    permissions = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
)
