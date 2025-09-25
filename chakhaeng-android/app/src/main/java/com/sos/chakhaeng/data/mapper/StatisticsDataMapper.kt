package com.sos.chakhaeng.data.mapper

import com.sos.chakhaeng.data.network.dto.response.statistics.*
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.statistics.*

object StatisticsDataMapper {

    fun ViolationStatisticsDTO.toEntity(): ViolationStatistics =
        ViolationStatistics(
            totalDetections = totalDetections,
            detectionAccuracy = detectionAccuracy,
            weeklyDetections = weeklyDetections,
            dailyAverageDetections = dailyAverageDetections,
            violationTypeStats = listOf(violationTypeStats.toEntity()),
            hourlyStats = listOf(hourlyStatistic.toEntity()),
            monthlyTrend = listOf(monthlyTrend.toEntity())
        )

    fun ReportStatisticsDTO.toEntity(): ReportStatistics =
        ReportStatistics(
            totalReports = totalReports,
            completedReports = completedReports,
            pendingReports = pendingReports,
            rejectedReports = rejectedReports,
            successRate = successRate,
            totalSuccessRate = totalSuccessRate,
            reportStatusStats = listOf(reportStatusStats.toEntity())
        )

    private fun ViolationTypeStatisticDTO.toEntity(): ViolationTypeStatistic =
        ViolationTypeStatistic(
            violationType = violationType.toViolationType(),
            count = count,
            percentage = percentage
        )

    private fun HourlyStatisticDTO.toEntity(): HourlyStatistic =
        HourlyStatistic(
            hour = hour,
            count = count
        )

    private fun MonthlyTrendDTO.toEntity(): MonthlyTrend =
        MonthlyTrend(
            month = month,
            count = count,
            changeFromPreviousMonth = changeFromPreviousMonth
        )

    private fun ReportStatusStatsDTO.toEntity(): ReportStatusStatistic =
        ReportStatusStatistic(
            status = status,
            count = count
        )

    private fun String.toViolationType(): ViolationType = when (this.uppercase()) {
        "WRONG_WAY" -> ViolationType.WRONG_WAY
        "SIGNAL" -> ViolationType.SIGNAL
        "LANE" -> ViolationType.LANE
        "NO_PLATE" -> ViolationType.NO_PLATE
        "NO_HELMET" -> ViolationType.NO_HELMET
        "OTHERS" -> ViolationType.OTHERS
        else -> ViolationType.OTHERS
    }
}