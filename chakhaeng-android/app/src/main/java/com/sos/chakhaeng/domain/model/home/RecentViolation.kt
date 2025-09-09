package com.sos.chakhaeng.domain.model.home

data class RecentViolation(
    val violationId: String,
    val type: String,
    val typeLabel: String,
    val location: String,
    val carNumber: String,
    val timestamp: Long,
)