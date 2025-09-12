package com.sos.chakhaeng.data.network.dto.response.reoprt

import com.google.gson.annotations.SerializedName

data class ReportItemDTO (
    @SerializedName("id")
    val id: String,

    @SerializedName("violationType")
    val violationType: String,

    @SerializedName("location")
    val location: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("plateNumber")
    val plateNumber: String,

    @SerializedName("occurredAt")
    val occurredAt: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdAt")
    val createdAt: String,
)

