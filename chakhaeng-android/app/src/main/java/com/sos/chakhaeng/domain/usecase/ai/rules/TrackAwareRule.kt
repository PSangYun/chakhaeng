package com.sos.chakhaeng.domain.usecase.ai.rules

import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.ai.TrackObj
import com.sos.chakhaeng.domain.model.violation.ViolationEvent

/**
 * 트래킹 결과(TrackObj)를 함께 사용해 프레임 단위로 위반을 판단하는 규칙.
 * frameW/H는 정규화가 필요할 때만 사용 (핵심 로직은 [0,1] LTRB 기준 권장).
 */
interface TrackAwareRule {
    fun evaluate(
        detections: List<Detection>,
        tracks: List<TrackObj>,
        frameW: Int,
        frameH: Int,
        nowMs: Long = System.currentTimeMillis()
    ): List<ViolationEvent>
}
