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
                totalDetections = 156,
                detectionAccuracy = 92,
                weeklyDetections = 28,
                dailyAverageDetections = 4.2,
                violationTypeStats = listOf(
                    ViolationTypeStatistic(ViolationType.SIGNAL, 65, 42),
                    ViolationTypeStatistic(ViolationType.LANE, 44, 28),
                    ViolationTypeStatistic(ViolationType.WRONG_WAY, 30, 19),
                    ViolationTypeStatistic(ViolationType.OTHERS, 17, 11)
                ),
                hourlyStats = createDummyHourlyStats(),
                monthlyTrend = listOf(
                    MonthlyTrend("10월", 70, 45),
                    MonthlyTrend("11월", 82, 52),
                    MonthlyTrend("12월", 76, 48),
                    MonthlyTrend("1월", 102, 65)
                )
            )
            Result.success(violationStats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReportStatistics(): Result<ReportStatistics> {
        return try {
            // API 호출 시뮬레이션을 위한 delay
            delay(800)

            val reportStats = ReportStatistics(
                totalReports = 43,
                completedReports = 38,
                pendingReports = 3,
                rejectedReports = 2,
                successRate = 88,
                totalSuccessRate = 82,
                reportStatusStats = listOf(
                    ReportStatusStatistic("처리 완료", 38),
                    ReportStatusStatistic("처리 중", 3),
                    ReportStatusStatistic("반려", 2)
                )
            )
            Result.success(reportStats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createDummyHourlyStats(): List<HourlyStatistic> {
        // 실제 교통량이 많은 시간대에 맞춰 더미 데이터 생성
        val baseData = listOf(
            2, 1, 1, 1, 2, 4, 8, 12, 15, 18, 14, 12,
            10, 8, 9, 11, 14, 18, 16, 12, 8, 6, 4, 3
        )

        return baseData.mapIndexed { hour, count ->
            HourlyStatistic(hour, count)
        }
    }
}