package com.sos.chakhaeng.data.network.dto.request.violation

import com.google.gson.annotations.SerializedName

data class DetectViolationRequest(
    @SerializedName("videoId") val videoId: String,
    @SerializedName("type") val type: String,
    @SerializedName("plate") val plate: String,
    @SerializedName("locationText") val locationText: String,
    @SerializedName("occurredAt") val occurredAt: String,
)