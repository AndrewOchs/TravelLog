package com.example.travellog.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travellog.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PhotoViewModel"

sealed class PhotoSaveState {
    data object Idle : PhotoSaveState()
    data object Saving : PhotoSaveState()
    data class Success(val photoId: Long) : PhotoSaveState()
    data class Error(val message: String) : PhotoSaveState()
}

@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _saveState = MutableStateFlow<PhotoSaveState>(PhotoSaveState.Idle)
    val saveState: StateFlow<PhotoSaveState> = _saveState.asStateFlow()

    fun savePhoto(
        photoUri: Uri,
        stateCode: String,
        stateName: String,
        cityName: String,
        capturedDate: Long
    ) {
        viewModelScope.launch {
            Log.d(TAG, "savePhoto called from ViewModel")
            Log.d(TAG, "  URI: $photoUri")
            Log.d(TAG, "  State: $stateCode - $stateName")
            Log.d(TAG, "  City: $cityName")

            _saveState.value = PhotoSaveState.Saving

            val result = photoRepository.savePhoto(
                photoUri = photoUri,
                stateCode = stateCode,
                stateName = stateName,
                cityName = cityName,
                capturedDate = capturedDate
            )

            _saveState.value = result.fold(
                onSuccess = { photoId ->
                    Log.d(TAG, "Photo saved successfully with ID: $photoId")
                    PhotoSaveState.Success(photoId)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to save photo: ${error.message}", error)
                    PhotoSaveState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }

    fun resetSaveState() {
        _saveState.value = PhotoSaveState.Idle
    }
}
