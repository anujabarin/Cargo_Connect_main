package com.example.cargolive.ui.cargo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargolive.data.models.Schedule
import com.example.cargolive.data.repository.CargoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CargoViewModel : ViewModel() {
    private val repository = CargoRepository()
    private val TAG = "CargoViewModel"

    private val _uiState = MutableStateFlow<CargoItemUiState>(CargoItemUiState.Loading)
    val uiState: StateFlow<CargoItemUiState> = _uiState.asStateFlow()

    fun loadScheduleDetails(scheduleId: String) {
        viewModelScope.launch {
            _uiState.value = CargoItemUiState.Loading
            try {
                Log.d(TAG, "Loading schedule details for ID: $scheduleId")
                val schedule = repository.getCargoItemById(scheduleId)
                if (schedule != null) {
                    Log.d(TAG, "Schedule retrieved successfully: ${schedule.cargoId}")
                    Log.d(TAG, "Schedule pickup location: ${schedule.pickupLocation}")
                    Log.d(TAG, "Schedule dropoff location: ${schedule.dropoffLocation}")
                    Log.d(TAG, "Schedule pickup time: ${schedule.pickupTime}")
                    _uiState.value = CargoItemUiState.Success(schedule)
                } else {
                    Log.e(TAG, "Schedule not found for ID: $scheduleId")
                    _uiState.value = CargoItemUiState.Error("Schedule not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading schedule details", e)
                _uiState.value = CargoItemUiState.Error("Failed to load schedule: ${e.message ?: "Unknown error"}")
            }
        }
    }
}

sealed class CargoItemUiState {
    object Loading : CargoItemUiState()
    data class Success(val schedule: Schedule) : CargoItemUiState()
    data class Error(val message: String) : CargoItemUiState()
}