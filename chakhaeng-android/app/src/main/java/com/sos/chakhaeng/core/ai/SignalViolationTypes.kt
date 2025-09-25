package com.sos.chakhaeng.core.ai

/**
 * 신호위반 로직이 반환하는 내부 DTO (정책 전용)
 * 1) tracking id, 2) 위반 시간, 3) 차 종류, 4) 번호판 텍스트(추후 OCR로 채움)
 */
data class SignalViolationHit(
    val trackId: Int,
    val whenMs: Long,
    val vehicleType: String,
    val plateText: String? = null
)