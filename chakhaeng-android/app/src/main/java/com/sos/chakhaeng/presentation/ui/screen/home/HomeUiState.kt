package com.sos.chakhaeng.presentation.ui.screen.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val esgScore: Int = 1250,
    val todayDetections: Int = 8,
    val recentReports: List<String> = emptyList()
)