package com.sos.chakhaeng.presentation.ui.screen.report

import com.sos.chakhaeng.domain.model.location.Location
import com.sos.chakhaeng.domain.model.report.ReportDetailItem

data class ReportDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val reportDetailItem: ReportDetailItem = ReportDetailItem(),

    // 지도 관련 상태
    val mapLocation: Location = Location.DEFAULT,
    val isMapLoading: Boolean = false,
    val mapError: String? = null
)
