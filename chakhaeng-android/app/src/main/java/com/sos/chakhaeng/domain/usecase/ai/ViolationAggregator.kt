// domain/usecase/ai/ViolationAggregator.kt
package com.sos.chakhaeng.domain.usecase.ai

import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import javax.inject.Inject

class ViolationAggregator @Inject constructor() {
    fun aggregate(events: List<ViolationEvent>): List<ViolationEvent> {
        // 단순 중복 제거(타입+소수점 반올림 confidence)
        return events
            .groupBy { it.type to (it.confidence * 100).toInt() }
            .map { (_, group) -> group.maxBy { it.confidence } }
    }
}
