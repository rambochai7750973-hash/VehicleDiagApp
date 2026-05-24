package com.vehiclediag.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vehiclediag.app.data.api.RetrofitClient
import com.vehiclediag.app.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val deviceIp: String = "http://192.168.4.1",
    val deviceSsid: String = "VehicleDiag",
    val devicePassword: String = "12345678",
    val isConnected: Boolean = false,
    val logText: String = "",
    val isLoadingLog: Boolean = false,
    val logError: String? = null,
    val connectionStatus: String = "",
)

class SettingsViewModel(
    private val repository: VehicleRepository = VehicleRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateDeviceIp(ip: String) {
        _uiState.value = _uiState.value.copy(deviceIp = ip)
        RetrofitClient.updateBaseUrl(ip)
    }

    fun updateSsid(ssid: String) {
        _uiState.value = _uiState.value.copy(deviceSsid = ssid)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(devicePassword = password)
    }

    fun connect() {
        val state = _uiState.value
        RetrofitClient.updateBaseUrl(state.deviceIp)
        _uiState.value = _uiState.value.copy(
            isConnected = true,
            connectionStatus = "已连接至 ${state.deviceSsid}",
        )
    }

    fun disconnect() {
        _uiState.value = _uiState.value.copy(
            isConnected = false,
            connectionStatus = "已断开",
        )
    }

    fun loadLog() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLog = true, logError = null)
            repository.getLog().onSuccess { log ->
                _uiState.value = _uiState.value.copy(
                    logText = log,
                    isLoadingLog = false,
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoadingLog = false,
                    logError = e.message,
                )
            }
        }
    }

    fun clearLog() {
        viewModelScope.launch {
            repository.clearLog().onSuccess {
                _uiState.value = _uiState.value.copy(logText = "")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(logError = e.message)
            }
        }
    }
}
