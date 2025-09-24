package com.sos.chakhaeng.domain.usecase.ai

import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.usecase.ai.rules.ViolationRule
import javax.inject.Inject

class ProcessDetectionsUseCase @Inject constructor(
    private val rules: Set<@JvmSuppressWildcards ViolationRule>,
    private val aggregator: ViolationAggregator
) {
    operator fun invoke(detections: List<Detection>): List<ViolationEvent> {
        if (detections.isEmpty()) return emptyList()
        val all = buildList {
            for (rule in rules) addAll(rule.evaluate(detections))
        }
        return aggregator.aggregate(all)
    }
}
