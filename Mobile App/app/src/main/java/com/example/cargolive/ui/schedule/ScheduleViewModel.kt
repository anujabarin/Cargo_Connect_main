package com.example.cargolive.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargolive.data.models.Schedule
import com.example.cargolive.data.repository.CargoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel : ViewModel() {
    private val repository = CargoRepository()

    private val _uiState = MutableStateFlow<ScheduleListUiState>(ScheduleListUiState.Loading)
    val uiState: StateFlow<ScheduleListUiState> = _uiState.asStateFlow()

    init {
        loadSchedules()
    }

    fun loadSchedules() {
        viewModelScope.launch {
            _uiState.value = ScheduleListUiState.Loading
            try {
                val schedules = repository.getSchedules()
                _uiState.value = ScheduleListUiState.Success(schedules)
            } catch (e: Exception) {
                _uiState.value = ScheduleListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class ScheduleListUiState {
    object Loading : ScheduleListUiState()
    data class Success(val schedules: List<Schedule>) : ScheduleListUiState()
    data class Error(val message: String) : ScheduleListUiState()
}