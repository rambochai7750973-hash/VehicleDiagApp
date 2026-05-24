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
    val connectionError: String? = null,
    val isTestingConnection: Boolean = false,
    val testResult: String = "",
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
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = _uiState.value.copy(
                isTestingConnection = true,
                connectionError = null,
                connectionStatus = "正在连接...",
            )
            RetrofitClient.updateBaseUrl(state.deviceIp)
            RetrofitClient.rawGet("/api/status").onSuccess { raw ->
                _uiState.value = _uiState.value.copy(
                    isConnected = true,
                    isTestingConnection = false,
                    connectionStatus = "已连接至 ${state.deviceSsid}",
                    connectionError = null,
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isConnected = false,
                    isTestingConnection = false,
                    connectionStatus = "连接失败",
                    connectionError = "设备无响应: ${e.message}",
                )
            }
        }
    }

    fun testApi() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingConnection = true, testResult = "测试中...")
            val results = mutableListOf<String>()
            // 测试多个关键端点
            for (endpoint in listOf("/api/status", "/api/protocol", "/api/pids/defs", "/api/ping", "/api/monitor")) {
                RetrofitClient.rawGet(endpoint).onSuccess { raw ->
                    results.add("=== $endpoint ===\n$raw\n")
                }.onFailure { e ->
                    results.add("=== $endpoint ===\n请求失败: ${e.message}\n")
                }
            }
            _uiState.value = _uiState.value.copy(
                isTestingConnection = false,
                testResult = results.joinToString("\n"),
            )
        }
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
