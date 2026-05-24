package com.vehiclediag.app.data.model

import com.google.gson.annotations.SerializedName

data class DeviceStatus(
    @SerializedName("tx") val tx: Int = 0,
    @SerializedName("rx") val rx: Int = 0,
    @SerializedName("busActive") val busActive: Boolean = false,
    @SerializedName("diagBusy") val diagBusy: Boolean = false,
    @SerializedName("protocol") val protocol: String? = null,
)
