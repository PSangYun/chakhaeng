package com.sos.chakhaeng.presentation.ui.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.statistics.HourlyStatistic
import com.sos.chakhaeng.domain.model.statistics.MonthlyTrend
import com.sos.chakhaeng.domain.model.statistics.ReportStatistics
import com.sos.chakhaeng.domain.model.statistics.ReportStatusStatistic
import com.sos.chakhaeng.domain.model.statistics.StatisticsTab
import com.sos.chakhaeng.domain.model.statistics.ViolationStatistics
import com.sos.chakhaeng.domain.model.statistics.ViolationTypeStatistic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(

) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun selectTab(tab: StatisticsTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    // 통계 데이터 로드 (현재는 더미데이터 사용)
    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.loading()

            try {
                // 실제 API 구현 전까지 더미 데이터 사용
                val violationStats = createDummyViolationStats()
                val reportStats = createDummyReportStats()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    violationStats = violationStats,
                    reportStats = reportStats
                )
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.error("통계 데이터 로드 실패: ${e.message}")
            }
        }
    }


    // 통계 새로고침
    fun refreshStatistics() {
        loadStatistics()
    }

    // 더미 위반 통계 데이터 생성
    private fun createDummyViolationStats(): ViolationStatistics {
        return ViolationStatistics(
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
    }

    // 더미 신고 통계 데이터 생성
    private fun createDummyReportStats(): ReportStatistics {
        return ReportStatistics(
            totalReports = 43,
            completedReports = 38,
            pendingReports = 3,
            rejectedReports = 2,
            successRate = 88,
            totalSuccessRate = 82,
            reportStatusStats = listOf(
                ReportStatusStatistic("처리 완료", 38, "#4CAF50"),
                ReportStatusStatistic("처리 중", 3, "#FF9800"),
                ReportStatusStatistic("반려", 2, "#F44336")
            )
        )
    }

    // 더미 시간대별 통계 생성
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