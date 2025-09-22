package com.sos.chakhaeng.data.network.dto.response.statistics

import com.google.gson.annotations.SerializedName

data class ReportStatisticsDTO(
    @SerializedName("totalReports")
    val totalReports: Int,

    @SerializedName("completedReports")
    val completedReports: Int,

    @SerializedName("pendingReports")
    val pendingReports: Int,

    @SerializedName("rejectedReports")
    val rejectedReports: Int,

    @SerializedName("successRate")
    val successRate: Int,

    @SerializedName("totalSuccessRate")
    val totalSuccessRate: Int,

    @SerializedName("reportStatusStats")
    val reportStatusStats: ReportStatusStatsDTO
)
