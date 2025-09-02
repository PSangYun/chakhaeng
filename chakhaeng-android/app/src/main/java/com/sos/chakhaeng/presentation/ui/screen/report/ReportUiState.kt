package com.sos.chakhaeng.presentation.ui.screen.report

data class ReportUiState(
    val reports: List<String> = emptyList(),
    val isLoading: Boolean = false
)