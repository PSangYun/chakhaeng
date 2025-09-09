package com.sos.chakhaeng.data.network.dto.response.home

import com.google.gson.annotations.SerializedName

data class RecentViolationDTO(
    @SerializedName("violationId")
    val violationId: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("typeLabel")
    val typeLabel: String,

    @SerializedName("plate")
    val plate: String,

    @SerializedName("locationText")
    val locationText: String,

    @SerializedName("occurredAt")
    val occurredAt: String,
)
