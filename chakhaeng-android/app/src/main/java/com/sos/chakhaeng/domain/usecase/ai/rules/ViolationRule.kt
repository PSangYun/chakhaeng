package com.sos.chakhaeng.domain.usecase.ai.rules

import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.ai.TrackObj
import com.sos.chakhaeng.domain.model.violation.ViolationEvent

interface ViolationRule {
    val name: String
    fun evaluate(detections: List<Detection>, tracks: List<TrackObj>): List<ViolationEvent>
}
