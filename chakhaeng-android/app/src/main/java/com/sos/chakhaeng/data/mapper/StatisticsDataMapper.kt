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
            violationTypeStats = violationTypeStats.map {
                it.toEntity()
            },
            hourlyStats = hourlyStatistic.map{it.toEntity()},
            monthlyTrend = monthlyTrend.map{it.toEntity()}
        )

    fun ReportStatisticsDTO.toEntity(): ReportStatistics =
        ReportStatistics(
            totalReports = totalReports,
            completedReports = completedReports,
            pendingReports = pendingReports,
            rejectedReports = rejectedReports,
            successRate = successRate,
            totalSuccessRate = totalSuccessRate,
            reportStatusStats = reportStatusStats.map{it.toEntity()}
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
        "역주행" -> ViolationType.WRONG_WAY
        "신호위반" -> ViolationType.SIGNAL
        "차선침범" -> ViolationType.LANE
        "무번호판" -> ViolationType.NO_PLATE
        "헬멧 미착용" -> ViolationType.NO_HELMET
        "OTHERS" -> ViolationType.OTHERS
        else -> ViolationType.OTHERS
    }
}