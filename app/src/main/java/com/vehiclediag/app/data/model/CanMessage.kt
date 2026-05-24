package com.vehiclediag.app.data.model

import com.google.gson.annotations.SerializedName

data class CanMessage(
    @SerializedName("id") val id: String,
    @SerializedName("rx") val rx: Boolean,
    @SerializedName("time") val time: Int,
    @SerializedName("data") val data: String,
)

data class MonitorResponse(
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("messages") val messages: List<CanMessage>,
)
