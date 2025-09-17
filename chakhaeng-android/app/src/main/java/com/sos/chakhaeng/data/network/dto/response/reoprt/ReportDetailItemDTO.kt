package com.sos.chakhaeng.data.network.dto.response.reoprt

import com.google.gson.annotations.SerializedName

data class ReportDetailItemDTO(
    @SerializedName("id")
    val id: String,

    @SerializedName("videoId")
    val videoId: String,

    @SerializedName("objectKey")
    val objectKey: String,

    @SerializedName("violationType")
    val violationType: String,

    @SerializedName("location")
    val location: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("plateNumber")
    val plateNumber: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("occurredAt")
    val occurredAt: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdAt")
    val createdAt: String,

)
