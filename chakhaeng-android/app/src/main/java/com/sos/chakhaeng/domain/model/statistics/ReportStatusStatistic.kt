package com.sos.chakhaeng.domain.model.statistics

data class ReportStatusStatistic(
    val status: String, // "처리중", "완료", "반려"
    val count: Int,
    val color: String // 차트 색상용
)
