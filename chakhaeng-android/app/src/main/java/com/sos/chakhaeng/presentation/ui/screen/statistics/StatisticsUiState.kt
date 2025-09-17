package com.sos.chakhaeng.presentation.ui.screen.statistics

import com.sos.chakhaeng.domain.model.statistics.StatisticsTab

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val selectedTab: StatisticsTab = StatisticsTab.VIOLATION_STATISTICS
)

