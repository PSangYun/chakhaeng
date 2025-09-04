package com.sos.chakhaeng.presentation.ui.model

data class RecentViolationUiModel(
    val id: Long,
    val type: String,
    val location: String,
    val carNumber: String,
    val timestamp: Long,
    val severity: ViolationSeverity
)

enum class ViolationSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}