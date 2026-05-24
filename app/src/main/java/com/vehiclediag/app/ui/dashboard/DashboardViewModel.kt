package com.vehiclediag.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vehiclediag.app.data.model.DeviceStatus
import com.vehiclediag.app.data.model.PidDef
import com.vehiclediag.app.data.model.PidLiveData
import com.vehiclediag.app.data.model.ProtocolList
import com.vehiclediag.app.data.repository.VehicleRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardUiState(
    val deviceStatus: DeviceStatus = DeviceStatus(),
    val pidDefs: List<PidDef> = emptyList(),
    val livePidData: List<PidLiveData> = emptyList(),
    val protocolList: ProtocolList? = null,
    val selectedProtocolIndex: Int = 0,
    val pingResult: String = "",
    val isLoading: Boolean = false,
    val isPidPolling: Boolean = false,
    val error: String? = null,
    val statusError: String? = null,
    val protocolError: String? = null,
    val pingError: String? = null,
)

class DashboardViewModel(
    private val repository: VehicleRepository = VehicleRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var statusJob: Job? = null

    fun initialize() {
        loadPidDefs()
        loadProtocols()
        startStatusPolling()
    }

    private fun startStatusPolling() {
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            while (true) {
                loadDeviceStatus()
                delay(3000)
            }
        }
    }

    private suspend fun loadDeviceStatus() {
        repository.getStatus().onSuccess { status ->
            _uiState.value = _uiState.value.copy(
                deviceStatus = status,
                statusError = null,
            )
        }.onFailure { e ->
            _uiState.value = _uiState.value.copy(
                statusError = e.message ?: "获取设备状态失败",
            )
        }
    }

    private fun loadPidDefs() {
        viewModelScope.launch {
            repository.getPidDefs().onSuccess { defs ->
                _uiState.value = _uiState.value.copy(pidDefs = defs)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "获取PID定义失败",
                )
            }
        }
    }

    private fun loadProtocols() {
        viewModelScope.launch {
            repository.getProtocols().onSuccess { protocols ->
                _uiState.value = _uiState.value.copy(
                    protocolList = protocols,
                    selectedProtocolIndex = protocols.current,
                    protocolError = null,
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(protocolError = e.message)
            }
        }
    }

    fun selectProtocol(index: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedProtocolIndex = index)
            repository.switchProtocol(index).onFailure { e ->
                _uiState.value = _uiState.value.copy(protocolError = e.message)
            }
        }
    }

    fun startPidPolling() {
        if (_uiState.value.isPidPolling) return
        val pidList = _uiState.value.pidDefs.map { it.pid }
        if (pidList.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "未加载到PID定义，请确认设备连接",
            )
            return
        }

        viewModelScope.launch {
            repository.startPidPolling(pidList).onSuccess {
                pollingJob?.cancel()
                pollingJob = viewModelScope.launch {
                    var failures = 0
                    while (true) {
                        repository.getLivePidData().onSuccess { data ->
                            _uiState.value = _uiState.value.copy(
                                livePidData = data,
                                isLoading = false,
                                error = null,
                            )
                            failures = 0
                            delay(2000)
                        }.onFailure { e ->
                            failures++
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message,
                            )
                            val backoff = (failures * 1000L).coerceAtMost(10000L)
                            delay(backoff)
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(isPidPolling = true, error = null)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun stopPidPolling() {
        pollingJob?.cancel()
        pollingJob = null
        _uiState.value = _uiState.value.copy(isPidPolling = false)

        viewModelScope.launch {
            repository.stopPidPolling().onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun pingBus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(pingResult = "检测中...", pingError = null)
            repository.ping().onSuccess { ping ->
                val status = if (ping.success) "ECU 响应正常: ${ping.status ?: "OK"}" else "无响应"
                _uiState.value = _uiState.value.copy(pingResult = status)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(pingResult = "检测失败", pingError = e.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        statusJob?.cancel()
        if (_uiState.value.isPidPolling) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.stopPidPolling()
            }
        }
    }
}
