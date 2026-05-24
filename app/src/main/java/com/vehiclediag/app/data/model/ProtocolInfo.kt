package com.vehiclediag.app.data.model

import com.google.gson.annotations.SerializedName

data class ProtocolList(
    @SerializedName("current") val current: Int,
    @SerializedName("names") val names: List<String>,
)

data class ProtocolSwitchRequest(
    @SerializedName("index") val index: Int,
)
