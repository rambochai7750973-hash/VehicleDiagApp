package com.vehiclediag.app.data.repository

import com.vehiclediag.app.data.api.RetrofitClient
import com.vehiclediag.app.data.model.*

class VehicleRepository {

    private val api get() = RetrofitClient.getApiService()

    suspend fun getStatus(): Result<DeviceStatus> = apiCall { api.getStatus() }
    suspend fun ping(): Result<PingResponse> = apiCall { api.ping() }
    suspend fun getProtocols(): Result<ProtocolList> = apiCall { api.getProtocols() }
    suspend fun switchProtocol(index: Int): Result<ApiResponse> =
        apiCall { api.switchProtocol(ProtocolSwitchRequest(index)) }

    suspend fun getPidDefs(): Result<List<PidDef>> = apiCall { api.getPidDefs() }
    suspend fun readSinglePid(pid: Int): Result<PidLiveData> =
        apiCall { api.readSinglePid(PidReadRequest(pid)) }
    suspend fun startPidPolling(pids: List<Int>): Result<ApiResponse> =
        apiCall { api.startPidPolling(PidStartRequest(pids)) }
    suspend fun stopPidPolling(): Result<ApiResponse> = apiCall { api.stopPidPolling() }
    suspend fun getLivePidData(): Result<List<PidLiveData>> = apiCall { api.getLivePidData() }

    suspend fun getStoredDtc(): Result<DtcStoredResponse> = apiCall { api.getStoredDtc() }
    suspend fun getPendingDtc(): Result<DtcPendingResponse> = apiCall { api.getPendingDtc() }
    suspend fun clearDtc(): Result<DtcClearResponse> = apiCall { api.clearDtc() }

    suspend fun getMonitorMessages(): Result<MonitorResponse> = apiCall { api.getMonitorMessages() }
    suspend fun startMonitor(): Result<ApiResponse> = apiCall { api.startMonitor() }
    suspend fun stopMonitor(): Result<ApiResponse> = apiCall { api.stopMonitor() }
    suspend fun clearMonitor(): Result<ApiResponse> = apiCall { api.clearMonitor() }

    suspend fun udsSessionControl(type: Int): Result<ApiResponse> =
        apiCall { api.udsSessionControl(UdsSessionRequest(type)) }
    suspend fun udsReset(type: Int): Result<ApiResponse> =
        apiCall { api.udsReset(UdsResetRequest(type)) }
    suspend fun udsReadDid(dataId: Int): Result<ApiResponse> =
        apiCall { api.udsReadDid(UdsReadRequest(dataId)) }
    suspend fun udsReadVin(): Result<UdsVinResponse> = apiCall { api.udsReadVin() }
    suspend fun sendCustomDiag(id: Int, data: List<Int>): Result<CustomDiagResponse> =
        apiCall { api.sendCustomDiag(CustomDiagRequest(id, data)) }

    suspend fun getLog(): Result<String> = apiCall { api.getLog() }
    suspend fun clearLog(): Result<ApiResponse> = apiCall { api.clearLog() }

    private suspend fun <T> apiCall(call: suspend () -> retrofit2.Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
