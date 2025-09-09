package com.sos.chakhaeng.presentation.model

import java.time.LocalDateTime

data class ViolationDetectionUiModel(
    val id: Long,
    val type: ViolationType,
    val licenseNumber: String,
    val location: String,
    val detectedAt: LocalDateTime,
    val confidence: Float, // 탐지 신뢰도 (0.0 ~ 1.0)
    val thumbnailUrl: String? = null,
    val confidencePercentage: Int,
    val timeAgo: String,
    val hasImage: Boolean
)