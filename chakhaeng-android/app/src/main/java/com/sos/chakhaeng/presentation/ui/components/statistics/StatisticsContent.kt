package com.sos.chakhaeng.presentation.ui.components.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.statistics.StatisticsTab
import com.sos.chakhaeng.presentation.ui.components.statistics.section.HourlyDistributionSection
import com.sos.chakhaeng.presentation.ui.components.statistics.section.MonthlyTrendSection
import com.sos.chakhaeng.presentation.ui.components.statistics.section.ReportStatisticsSection
import com.sos.chakhaeng.presentation.ui.components.statistics.section.StatisticsCardsSection
import com.sos.chakhaeng.presentation.ui.components.statistics.section.StatisticsTabSection
import com.sos.chakhaeng.presentation.ui.components.statistics.section.ViolationDistributionSection
import com.sos.chakhaeng.presentation.ui.screen.statistics.StatisticsUiState

@Composable
fun StatisticsContent(
    uiState: StatisticsUiState,
    onTabSelected: (StatisticsTab) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StatisticsTabSection(
                selectedTab = uiState.selectedTab,
                onTabSelected = onTabSelected
            )
        }

        when (uiState.selectedTab) {
            StatisticsTab.VIOLATION_STATISTICS -> {
                // 위반 탐지 통계
                if (uiState.violationStats == null) {
                    item {
                        Text("위반 탐지먼저 해라")
                    }
                } else {
                    val stats = uiState.violationStats
                    item {
                        StatisticsCardsSection(
                            totalDetections = stats.totalDetections,
                            accuracy = stats.detectionAccuracy,
                            weeklyDetections = stats.weeklyDetections,
                            dailyAverage = stats.dailyAverageDetections
                        )
                    }

                    // 위반 유형별 분포 차트
                    item {
                        ViolationDistributionSection(
                            violationStats = stats.violationTypeStats,
                            totalCount = stats.totalDetections
                        )
                    }

                    // 시간대별 위반 발생 차트
                    item {
                        HourlyDistributionSection(
                            hourlyStats = stats.hourlyStats
                        )
                    }

                    // 월별 트렌드
                    item {
                        MonthlyTrendSection(
                            monthlyTrend = stats.monthlyTrend
                        )
                    }
                }
            }

            StatisticsTab.REPORT_STATISTICS -> {
                // 신고 통계
                uiState.reportStats?.let { stats ->
                    item {
                        ReportStatisticsSection(reportStats = stats)
                    }
                }
            }
        }
    }
}