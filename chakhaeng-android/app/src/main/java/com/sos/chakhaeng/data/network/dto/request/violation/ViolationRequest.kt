package com.sos.chakhaeng.data.network.dto.request.violation

import com.google.gson.annotations.SerializedName

data class ViolationRequest(
    @SerializedName("violationType") val violationType: String,
    @SerializedName("location")      val location: String,
    @SerializedName("title")         val title: String,
    @SerializedName("description")   val description: String,
    @SerializedName("plateNumber")   val plateNumber: String,
    @SerializedName("date")          val date: String,   // yyyy-MM-dd
    @SerializedName("time")          val time: String,   // HH:mm:ss
    @SerializedName("videoId")      val videoId: String?
)
