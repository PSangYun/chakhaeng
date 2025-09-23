package com.sos.chakhaeng.domain.model.statistics

data class ReportStatistics(
    val totalReports: Int,
    val completedReports: Int,
    val pendingReports: Int,
    val rejectedReports: Int,
    val successRate: Int, // 신고 성공률 (%)
    val totalSuccessRate: Int, // 서비스 이용자 전체 신고 성공률 (%)

    val reportStatusStats: List<ReportStatusStatistic>
)
