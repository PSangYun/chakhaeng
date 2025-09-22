package com.sos.chakhaeng.domain.model.statistics

data class MonthlyTrend(
    val month: String,
    val count: Int,
    val percentage: Int // 전월 대비 증감률
)
