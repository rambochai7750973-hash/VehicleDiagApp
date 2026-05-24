package com.vehiclediag.app.data.model

import com.google.gson.annotations.SerializedName

data class PidDef(
    @SerializedName("pid") val pid: Int,
    @SerializedName("name") val name: String,
    @SerializedName("unit") val unit: String,
)

data class PidLiveData(
    @SerializedName("pid") val pid: Int,
    @SerializedName("valid") val valid: Boolean,
    @SerializedName("value") val value: Float,
    @SerializedName("name") val name: String,
    @SerializedName("unit") val unit: String,
)

data class PidReadRequest(
    @SerializedName("pid") val pid: Int,
)

data class PidStartRequest(
    @SerializedName("pids") val pids: List<Int>,
)
