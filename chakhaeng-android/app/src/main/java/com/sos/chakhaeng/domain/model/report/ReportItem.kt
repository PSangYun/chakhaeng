package com.sos.chakhaeng.domain.model.report

import com.sos.chakhaeng.domain.model.ViolationType

data class ReportItem(
    val id: String,
    val violationType: ViolationType,
    val location: String,
    val videoFileName: String,
    val plateNumber: String,
    val occurredAt: Long,
    val status: ReportStatus,
    val createdAt: Long,
)
