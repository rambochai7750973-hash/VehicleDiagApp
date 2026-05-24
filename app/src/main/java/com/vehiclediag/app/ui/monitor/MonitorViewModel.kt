package com.vehiclediag.app.ui.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vehiclediag.app.data.model.CanMessage
import com.vehiclediag.app.data.repository.VehicleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MonitorUiState(
    val messages: List<CanMessage> = emptyList(),
    val isMonitoring: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class MonitorViewModel(
    private val repository: VehicleRepository = VehicleRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    fun startMonitoring() {
        viewModelScope.launch {
            repository.startMonitor().onSuccess {
                _uiState.value = _uiState.value.copy(
                    isMonitoring = true,
                    error = null,
                )
                startPolling()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun stopMonitoring() {
        pollJob?.cancel()
        pollJob = null

        viewModelScope.launch {
            repository.stopMonitor().onSuccess {
                _uiState.value = _uiState.value.copy(isMonitoring = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            repository.clearMonitor().onSuccess {
                _uiState.value = _uiState.value.copy(messages = emptyList())
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                repository.getMonitorMessages().onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        messages = response.messages,
                        isLoading = false,
                        error = null,
                    )
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message,
                    )
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
