package com.vehiclediag.app.data.model

import com.google.gson.annotations.SerializedName

data class UdsSessionRequest(
    @SerializedName("type") val type: Int,
)

data class UdsResetRequest(
    @SerializedName("type") val type: Int,
)

data class UdsReadRequest(
    @SerializedName("dataId") val dataId: Int,
)

data class UdsVinResponse(
    @SerializedName("vin") val vin: String,
)

data class CustomDiagRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("data") val data: List<Int>,
)

data class CustomDiagResponse(
    @SerializedName("id") val id: Int?,
    @SerializedName("data") val data: List<Int>?,
)

data class PingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("status") val status: String?,
)

data class ApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
)
