package com.sos.chakhaeng.domain.usecase.ai.rules

import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.ai.TrackObj
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.usecase.ai.geom.iou01
import javax.inject.Inject
import kotlin.math.min

data class CrosswalkConfig(
    val carLabels: List<String> = listOf("car","truck","bus"),
    val crosswalkLabels: List<String> = listOf("crosswalk","zebra_crossing"),
    val minCarScore: Float = 0.45f,
    val minCrosswalkScore: Float = 0.50f,
    val invadeIou: Float = 0.30f,
)

class CrosswalkInvadeRule @Inject constructor(
    private val cfg: CrosswalkConfig,
) : ViolationRule {

    override val name: String = "CrosswalkInvade"

    override fun evaluate(detections: List<Detection>, tracks: List<TrackObj>): List<ViolationEvent> {
        fun isCar(x: Detection) = cfg.carLabels.any { x.label.equals(it, true) } && x.score >= cfg.minCarScore
        fun isCrosswalk(x: Detection) = cfg.crosswalkLabels.any { x.label.contains(it, true) } && x.score >= cfg.minCrosswalkScore

        val cars = detections.filter(::isCar)
        val crosswalks = detections.filter(::isCrosswalk)
        if (cars.isEmpty() || crosswalks.isEmpty()) return emptyList()

        val out = mutableListOf<ViolationEvent>()
        for (c in cars) for (cw in crosswalks) {
            if (iou01(c.box, cw.box) >= cfg.invadeIou) {
                out += ViolationEvent(type = "CROSSWALK_INVADE", confidence = min(c.score, cw.score))
            }
        }
        return out
    }
}