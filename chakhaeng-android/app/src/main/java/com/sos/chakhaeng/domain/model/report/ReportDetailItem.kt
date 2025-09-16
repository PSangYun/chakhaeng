package com.sos.chakhaeng.domain.model.report

data class ReportDetailItem(
    val id: String = "",
    // Api가 아직 구현이 안되어서 Mock 데이터 임의 삽입
    val videoUrl: String = "814717a6-5f1d-4fe2-8937-4582560658a6",
    val reportState: ReportStatus = ReportStatus.PROCESSING,
    val violationType: String = "신호위반",
    val plateNumber: String = "12가1234",
    val occurredAt: Long = 0L,
    val location: String = "강남구 테헤란로 123",
    val reportContent: String = "신호위반 위반이 감지되었습니다. 차량번호 12가1234이(가) 강남구 테헤란로 123에서 위반행위를 하였습니다. 위반 차량이 명백한 교통법규 위반 행위를 하였으며, 블랙박스를 통해 명확한 증거가 확보되었습니다. 해당 위반에 대한 적절한 조치를 요청드립니다."
)
