package com.sos.chakhaeng.data.repository

import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.statistics.HourlyStatistic
import com.sos.chakhaeng.domain.model.statistics.MonthlyTrend
import com.sos.chakhaeng.domain.model.statistics.ReportStatistics
import com.sos.chakhaeng.domain.model.statistics.ReportStatusStatistic
import com.sos.chakhaeng.domain.model.statistics.ViolationStatistics
import com.sos.chakhaeng.domain.model.statistics.ViolationTypeStatistic
import com.sos.chakhaeng.domain.repository.StatisticsRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeStatisticsRepositoryImpl @Inject constructor() : StatisticsRepository {

    override suspend fun getViolationStatistics(): Result<ViolationStatistics> {
        return try {

            val violationStats = ViolationStatistics(
                totalDetections = 32,
                detectionAccuracy = 89,
                weeklyDetections = 23,
                dailyAverageDetections = 1.4,
                violationTypeStats = listOf(
                    ViolationTypeStatistic(ViolationType.NO_HELMET, 13, 41),
                    ViolationTypeStatistic(ViolationType.SIGNAL, 9, 28),
                    ViolationTypeStatistic(ViolationType.LANE, 7, 22),
                    ViolationTypeStatistic(ViolationType.OTHERS, 3, 9)
                ),
                hourlyStats = createDummyHourlyStats(),
                monthlyTrend = listOf(
                    MonthlyTrend("9월", 32, 0),
                )
            )
            Result.success(violationStats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReportStatistics(): Result<ReportStatistics> {
        return try {
            delay(800)

            val reportStats = ReportStatistics(
                totalReports = 32,
                completedReports = 28,
                pendingReports = 2,
                rejectedReports = 2,
                successRate = 88,  // 28/32 = 87.5% ≈ 88%
                totalSuccessRate = 82,
                reportStatusStats = listOf(
                    ReportStatusStatistic("처리 완료", 28),
                    ReportStatusStatistic("처리 중", 2),
                    ReportStatusStatistic("반려", 2)
                )
            )
            Result.success(reportStats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createDummyHourlyStats(): List<HourlyStatistic> {
        val baseData = listOf(
            2, 1, 1, 1, 2, 4, 8, 12, 13, 15, 14, 12,
            10, 8, 9, 11, 14, 18, 16, 12, 8, 6, 4, 3
        )

        return baseData.mapIndexed { hour, count ->
            HourlyStatistic(hour, count)
        }
    }
}