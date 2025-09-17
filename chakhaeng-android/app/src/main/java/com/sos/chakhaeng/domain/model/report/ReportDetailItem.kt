package com.sos.chakhaeng.domain.model.report

import com.sos.chakhaeng.domain.model.ViolationType

data class ReportDetailItem(
    val id: String = "",
    val videoId: String = "",
    val objectKey: String = "",
    val reportState: ReportStatus = ReportStatus.PROCESSING,
    val violationType: ViolationType = ViolationType.NO_HELMET,
    val location: String = "",
    val title: String = "",
    val plateNumber: String = "",
    val occurredAt: Long = 0L,
    val createdAt: Long = 0L,
    val reportContent: String = ""
)
