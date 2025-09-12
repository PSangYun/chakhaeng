package com.sos.chakhaeng.presentation.ui.screen.report

import com.sos.chakhaeng.domain.model.report.ReportItem
import com.sos.chakhaeng.domain.model.report.ReportTab

data class ReportUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val selectedTab: ReportTab = ReportTab.ALL,
    val reportItems: List<ReportItem> = emptyList(),
)