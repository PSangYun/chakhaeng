package com.sos.chakhaeng.data.network.dto.response.home

import com.google.gson.annotations.SerializedName

data class TodayStatsDTO(
    @SerializedName("todayDetected")
    val todayDetected: Int,

    @SerializedName("todayReported")
    val todayReported: Int
)
