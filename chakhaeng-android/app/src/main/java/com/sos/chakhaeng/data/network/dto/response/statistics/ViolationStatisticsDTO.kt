package com.sos.chakhaeng.data.network.dto.response.statistics

import com.google.gson.annotations.SerializedName

data class ViolationStatisticsDTO (
    @SerializedName("totalDetections")
    val totalDetections: Int,

    @SerializedName("detectionAccuracy")
    val detectionAccuracy: Int,

    @SerializedName("weeklyDetections")
    val weeklyDetections: Int,

    @SerializedName("dailyAverageDetections")
    val dailyAverageDetections: Double,

    @SerializedName("violationTypeStats")
    val violationTypeStats: ViolationTypeStatisticDTO,

    @SerializedName("hourlyStatistic")
    val hourlyStatistic: HourlyStatisticDTO,

    @SerializedName("monthlyTrend")
    val monthlyTrend: MonthlyTrendDTO
)