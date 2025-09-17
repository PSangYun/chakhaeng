package com.sos.chakhaeng.data.mapper

import com.sos.chakhaeng.data.network.dto.response.reoprt.ReportDetailItemDTO
import com.sos.chakhaeng.data.network.dto.response.reoprt.ReportItemDTO
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.report.ReportDetailItem
import com.sos.chakhaeng.domain.model.report.ReportItem
import com.sos.chakhaeng.domain.model.report.ReportStatus
import java.time.Instant
import kotlin.String

object ReportDataMapper {
    fun ReportItemDTO.toEntity(): ReportItem =
        ReportItem(
            id = id,
            violationType = violationType.toViolationType(),
            location = location,
            videoFileName = title,
            plateNumber = plateNumber,
            occurredAt = Instant.parse(occurredAt).toEpochMilli(),
            status = status.toReportStatus(),
            createdAt = Instant.parse(createdAt).toEpochMilli(),
        )

    fun ReportDetailItemDTO.toEntity(): ReportDetailItem =
        ReportDetailItem(
            id = id,
            videoId = videoId,
            objectKey = objectKey,
            reportState = status.toReportStatus(),
            violationType = violationType.toViolationType(),
            location = location,
            title = title,
            plateNumber = plateNumber,
            occurredAt = Instant.parse(occurredAt).toEpochMilli(),
            createdAt = Instant.parse(createdAt).toEpochMilli(),
            reportContent = description
        )
    
    private fun String.toViolationType(): ViolationType = when (this.uppercase()) {
        "WRONG_WAY" -> ViolationType.WRONG_WAY
        "SIGNAL" -> ViolationType.SIGNAL
        "LANE" -> ViolationType.LANE
        "NO_PLATE" -> ViolationType.NO_PLATE
        "NO_HELMET" -> ViolationType.NO_HELMET
        "OTHERS" -> ViolationType.OTHERS
        else -> ViolationType.OTHERS
    }
    
    private fun String.toReportStatus(): ReportStatus = when (this.uppercase()) {
        "PENDING" -> ReportStatus.PROCESSING
        "COMPLETED" -> ReportStatus.COMPLETED
        "REJECTED" -> ReportStatus.REJECTED
        else -> ReportStatus.PROCESSING
    }
}