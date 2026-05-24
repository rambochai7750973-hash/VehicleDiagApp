package com.vehiclediag.app.data.api

import com.vehiclediag.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("/api/status")
    suspend fun getStatus(): Response<DeviceStatus>

    @GET("/api/ping")
    suspend fun ping(): Response<PingResponse>

    @GET("/api/protocol")
    suspend fun getProtocols(): Response<ProtocolList>

    @POST("/api/protocol")
    suspend fun switchProtocol(@Body request: ProtocolSwitchRequest): Response<ApiResponse>

    @GET("/api/pids/defs")
    suspend fun getPidDefs(): Response<List<PidDef>>

    @POST("/api/pid/read")
    suspend fun readSinglePid(@Body request: PidReadRequest): Response<PidLiveData>

    @POST("/api/pids/start")
    suspend fun startPidPolling(@Body request: PidStartRequest): Response<ApiResponse>

    @POST("/api/pids/stop")
    suspend fun stopPidPolling(): Response<ApiResponse>

    @GET("/api/pids/live")
    suspend fun getLivePidData(): Response<List<PidLiveData>>

    @GET("/api/dtc/stored")
    suspend fun getStoredDtc(): Response<DtcStoredResponse>

    @GET("/api/dtc/pending")
    suspend fun getPendingDtc(): Response<DtcPendingResponse>

    @POST("/api/dtc/clear")
    suspend fun clearDtc(): Response<DtcClearResponse>

    @GET("/api/freezeframe")
    suspend fun getFreezeFrame(): Response<Any>

    @GET("/api/monitor")
    suspend fun getMonitorMessages(): Response<MonitorResponse>

    @POST("/api/monitor/start")
    suspend fun startMonitor(): Response<ApiResponse>

    @POST("/api/monitor/stop")
    suspend fun stopMonitor(): Response<ApiResponse>

    @POST("/api/monitor/clear")
    suspend fun clearMonitor(): Response<ApiResponse>

    @POST("/api/uds/session")
    suspend fun udsSessionControl(@Body request: UdsSessionRequest): Response<ApiResponse>

    @POST("/api/uds/reset")
    suspend fun udsReset(@Body request: UdsResetRequest): Response<ApiResponse>

    @POST("/api/uds/read")
    suspend fun udsReadDid(@Body request: UdsReadRequest): Response<ApiResponse>

    @GET("/api/uds/vin")
    suspend fun udsReadVin(): Response<UdsVinResponse>

    @POST("/api/diag/custom")
    suspend fun sendCustomDiag(@Body request: CustomDiagRequest): Response<CustomDiagResponse>

    @GET("/api/log")
    suspend fun getLog(): Response<String>

    @POST("/api/log/clear")
    suspend fun clearLog(): Response<ApiResponse>
}
