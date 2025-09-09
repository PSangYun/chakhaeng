package com.sos.chakhaeng.presentation.ui.screen.home

import com.sos.chakhaeng.domain.model.home.RecentViolation
import com.sos.chakhaeng.domain.model.home.TodayStats

data class HomeUiState(
    val esgScore: Int = 0,
    val todayStats: TodayStats = TodayStats(0, 0),
    val recentViolations: List<RecentViolation> = emptyList(),
    val showDetectionDialog: Boolean = false,
    val showStopDetectionDialog: Boolean = false,
    val error: String? = null,

    val isDetectionActive: Boolean = false,
    val isLoading: Boolean = false
)