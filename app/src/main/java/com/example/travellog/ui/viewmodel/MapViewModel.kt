package com.example.travellog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travellog.data.models.UsState
import com.example.travellog.data.models.allUsStates
import com.example.travellog.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    photoRepository: PhotoRepository
) : ViewModel() {

    val states: StateFlow<List<UsState>> = photoRepository.getStatePhotoCounts()
        .map { statePhotoCounts ->
            // Create a map of state codes to photo counts
            val photoCountMap = statePhotoCounts.associate { it.stateCode to it.photoCount }

            // Update all US states with their photo counts
            allUsStates.map { state ->
                state.copy(photoCount = photoCountMap[state.code] ?: 0)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = allUsStates // Start with empty photo counts
        )
}
