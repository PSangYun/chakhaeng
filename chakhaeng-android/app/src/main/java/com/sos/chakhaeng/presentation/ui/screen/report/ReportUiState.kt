package com.sos.chakhaeng.presentation.ui.screen.report

import com.sos.chakhaeng.domain.model.report.ReportItem
import com.sos.chakhaeng.domain.model.report.ReportTab

/**
 * 신고 현황 화면의 UI 상태
 */
data class ReportUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val selectedTab: ReportTab = ReportTab.ALL,
    val reportList: List<ReportItem> = emptyList()
)