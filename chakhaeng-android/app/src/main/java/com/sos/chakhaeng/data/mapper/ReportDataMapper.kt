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
        "역주행" -> ViolationType.WRONG_WAY
        "신호위반" -> ViolationType.SIGNAL
        "차선침범" -> ViolationType.LANE
        "킥보드 2인이상" -> ViolationType.LOVE_BUG
        "무번호판" -> ViolationType.NO_PLATE
        "헬멧 미착용" -> ViolationType.NO_HELMET
        "기타" -> ViolationType.OTHERS
        else -> ViolationType.OTHERS
    }
    
    private fun String.toReportStatus(): ReportStatus = when (this.uppercase()) {
        "PENDING" -> ReportStatus.PROCESSING
        "COMPLETED" -> ReportStatus.COMPLETED
        "REJECTED" -> ReportStatus.REJECTED
        else -> ReportStatus.PROCESSING
    }
}