package com.sos.chakhaeng.presentation.ui.mapper

import com.sos.chakhaeng.presentation.ui.model.ViolationDetectionUiModel
import com.sos.chakhaeng.presentation.ui.model.ViolationType
import java.time.Duration
import java.time.LocalDateTime

object ViolationUiMapper {

    fun mapToUiModel(
        id: Long,
        type: ViolationType,
        licenseNumber: String,
        location: String,
        detectedAt: LocalDateTime,
        confidence: Float,
        thumbnailUrl: String? = null,
        now: LocalDateTime = LocalDateTime.now()
    ): ViolationDetectionUiModel {
        return ViolationDetectionUiModel(
            id = id,
            type = type,
            licenseNumber = licenseNumber,
            location = location,
            detectedAt = detectedAt,
            confidence = confidence,
            thumbnailUrl = thumbnailUrl,
            confidencePercentage = calculateConfidencePercentage(confidence),
            timeAgo = formatTimeAgo(detectedAt, now),
            hasImage = hasImage(thumbnailUrl)
        )
    }

    private fun calculateConfidencePercentage(confidence: Float): Int {
        return (confidence * 100).toInt()
    }

    private fun formatTimeAgo(detectedAt: LocalDateTime, now: LocalDateTime): String {
        val duration = Duration.between(detectedAt, now)

        return when {
            duration.toMinutes() < 1 -> "방금 전"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}분 전"
            duration.toHours() < 24 -> "${duration.toHours()}시간 전"
            else -> "${duration.toDays()}일 전"
        }
    }

    private fun hasImage(thumbnailUrl: String?): Boolean {
        return !thumbnailUrl.isNullOrEmpty()
    }
}