package com.vehiclediag.app.data.model

import com.google.gson.annotations.SerializedName

data class CanMessage(
    @SerializedName("id") val id: Int,
    @SerializedName("rx") val rx: Boolean,
    @SerializedName("time") val time: String,
    @SerializedName("data") val data: List<Int>,
)

data class MonitorResponse(
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("messages") val messages: List<CanMessage>,
)
