package com.sos.chakhaeng.domain.model.statistics

data class ViolationStatistics(
    val totalDetections: Int,
    val detectionAccuracy: Int, // 정확도 (%)
    val weeklyDetections: Int,
    val dailyAverageDetections: Double,

    val violationTypeStats: List<ViolationTypeStatistic>,
    val hourlyStats: List<HourlyStatistic>,
    val monthlyTrend: List<MonthlyTrend>
)
