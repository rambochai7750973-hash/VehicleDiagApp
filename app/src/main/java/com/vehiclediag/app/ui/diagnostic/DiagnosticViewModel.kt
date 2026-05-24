package com.vehiclediag.app.ui.diagnostic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vehiclediag.app.data.model.CustomDiagResponse
import com.vehiclediag.app.data.model.DtcEntry
import com.vehiclediag.app.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiagnosticUiState(
    val storedDtc: List<DtcEntry> = emptyList(),
    val pendingDtc: List<DtcEntry> = emptyList(),
    val isReadingStoredDtc: Boolean = false,
    val isReadingPendingDtc: Boolean = false,
    val isClearingDtc: Boolean = false,
    val dtcClearSuccess: Boolean = false,
    val dtcError: String? = null,

    val udsSessionResult: String = "",
    val udsResetResult: String = "",
    val udsVinResult: String = "",
    val udsDidResult: String = "",
    val udsError: String? = null,
    val isReadingVin: Boolean = false,
    val isReadingDid: Boolean = false,

    val customDiagResponse: CustomDiagResponse? = null,
    val customDiagError: String? = null,
    val isSendingCustom: Boolean = false,
)

class DiagnosticViewModel(
    private val repository: VehicleRepository = VehicleRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticUiState())
    val uiState: StateFlow<DiagnosticUiState> = _uiState.asStateFlow()

    fun readStoredDtc() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isReadingStoredDtc = true,
                dtcError = null,
                dtcClearSuccess = false,
            )
            repository.getStoredDtc().onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    storedDtc = response.dtcs,
                    isReadingStoredDtc = false,
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isReadingStoredDtc = false,
                    dtcError = e.message,
                )
            }
        }
    }

    fun readPendingDtc() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isReadingPendingDtc = true,
                dtcError = null,
            )
            repository.getPendingDtc().onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    pendingDtc = response.dtcs,
                    isReadingPendingDtc = false,
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isReadingPendingDtc = false,
                    dtcError = e.message,
                )
            }
        }
    }

    fun clearDtc() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isClearingDtc = true,
                dtcError = null,
                dtcClearSuccess = false,
            )
            repository.clearDtc().onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isClearingDtc = false,
                    dtcClearSuccess = response.success,
                    storedDtc = emptyList(),
                    pendingDtc = emptyList(),
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isClearingDtc = false,
                    dtcError = e.message,
                )
            }
        }
    }

    fun udsSessionControl(type: Int) {
        val name = if (type == 1) "默认会话" else "扩展会话"
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(udsError = null)
            repository.udsSessionControl(type).onSuccess {
                _uiState.value = _uiState.value.copy(udsSessionResult = "$name 切换成功")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(udsError = e.message)
            }
        }
    }

    fun udsReset(type: Int) {
        val name = if (type == 1) "硬复位" else "软复位"
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(udsError = null)
            repository.udsReset(type).onSuccess {
                _uiState.value = _uiState.value.copy(udsResetResult = "$name 执行成功")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(udsError = e.message)
            }
        }
    }

    fun readVin() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReadingVin = true, udsError = null)
            repository.udsReadVin().onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    udsVinResult = response.vin,
                    isReadingVin = false,
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isReadingVin = false,
                    udsError = e.message,
                )
            }
        }
    }

    fun readDid(dataId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReadingDid = true, udsError = null)
            repository.udsReadDid(dataId).onSuccess {
                _uiState.value = _uiState.value.copy(
                    udsDidResult = "DID 0x${dataId.toString(16).uppercase()} 读取成功",
                    isReadingDid = false,
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isReadingDid = false,
                    udsError = e.message,
                )
            }
        }
    }

    fun sendCustomDiag(hexInput: String) {
        val bytes = parseHexString(hexInput)
        if (bytes.isEmpty()) {
            _uiState.value = _uiState.value.copy(customDiagError = "请输入有效的十六进制数据")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSendingCustom = true,
                customDiagError = null,
                customDiagResponse = null,
            )
            repository.sendCustomDiag(0x7E0, bytes).onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    customDiagResponse = response,
                    isSendingCustom = false,
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSendingCustom = false,
                    customDiagError = e.message,
                )
            }
        }
    }

    private fun parseHexString(input: String): List<Int> {
        return try {
            input.trim().split("\\s+".toRegex())
                .filter { it.isNotEmpty() }
                .map { it.toInt(16) }
        } catch (e: NumberFormatException) {
            emptyList()
        }
    }
}
