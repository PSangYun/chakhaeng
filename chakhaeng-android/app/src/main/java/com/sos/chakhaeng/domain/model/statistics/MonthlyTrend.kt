package com.sos.chakhaeng.domain.model.statistics

data class MonthlyTrend(
    val month: String,
    val count: Int,
    val changeFromPreviousMonth: Int
)
