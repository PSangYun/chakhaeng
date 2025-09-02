package com.sos.chakhaeng.presentation.ui.screen.statistics

data class StatisticsUiState(
    val totalDetections: Int = 156,
    val accuracy: Int = 92,
    val monthlyStats: Map<String, Int> = emptyMap()
)

