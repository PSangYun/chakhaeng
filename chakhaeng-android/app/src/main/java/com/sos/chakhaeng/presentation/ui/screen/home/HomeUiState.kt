package com.sos.chakhaeng.presentation.ui.screen.home

import com.sos.chakhaeng.presentation.ui.model.RecentViolationUiModel
import com.sos.chakhaeng.presentation.ui.model.TodayInfoUiModel

data class HomeUiState(
    val esgScore: Int = 0,
    val todayInfo: TodayInfoUiModel = TodayInfoUiModel(0, 0),
    val recentViolations: List<RecentViolationUiModel> = emptyList(),
    val showDetectionDialog: Boolean = false,
    val showStopDetectionDialog: Boolean = false,
    val error: String? = null,

    val isDetectionActive: Boolean = false,
    val isLoading: Boolean = false
)