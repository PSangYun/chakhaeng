package com.sos.chakhaeng.presentation.ui.screen.statistics

import com.sos.chakhaeng.domain.model.statistics.ReportStatistics
import com.sos.chakhaeng.domain.model.statistics.StatisticsTab
import com.sos.chakhaeng.domain.model.statistics.ViolationStatistics

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val selectedTab: StatisticsTab = StatisticsTab.VIOLATION_STATISTICS,
    val violationStats: ViolationStatistics? = null,
    val reportStats: ReportStatistics? = null
) {
    companion object {
        fun initial() = StatisticsUiState()
        fun loading() = StatisticsUiState(isLoading = true)
        fun error(message: String) = StatisticsUiState(error = message)
    }
}

