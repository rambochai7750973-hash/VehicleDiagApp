package com.vehiclediag.app.data.model

import com.google.gson.annotations.SerializedName

data class DtcEntry(
    @SerializedName("code") val code: String,
    @SerializedName("desc") val desc: String,
)

data class DtcStoredResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("dtcs") val dtcs: List<DtcEntry>,
)

data class DtcPendingResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("dtcs") val dtcs: List<DtcEntry>,
)

data class DtcClearResponse(
    @SerializedName("success") val success: Boolean,
)
