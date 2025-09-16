package com.sos.chakhaeng.domain.model.report

import com.sos.chakhaeng.domain.model.ViolationType

data class ReportDetailItem(
    val id: String = "",
    // Api가 아직 구현이 안되어서 Mock 데이터 임의 삽입
    val videoUrl: String = "814717a6-5f1d-4fe2-8937-4582560658a6",
    val reportState: ReportStatus = ReportStatus.PROCESSING,
    val violationType: ViolationType = ViolationType.NO_HELMET,
    val plateNumber: String = "12가1234",
    val occurredAt: Long = 0L,
    val location: String = "",
    val createdAt: Long = 0L,
    val reportContent: String = ""
)
