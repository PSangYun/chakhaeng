package com.sos.chakhaeng.data.network.dto.response.statistics

import com.google.gson.annotations.SerializedName

data class ReportStatusStatsDTO(
    @SerializedName("status")
    val status: String,
    @SerializedName("count")
    val count: Int
)
