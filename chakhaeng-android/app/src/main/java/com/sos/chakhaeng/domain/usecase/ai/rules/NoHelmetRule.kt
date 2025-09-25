package com.sos.chakhaeng.domain.usecase.ai.rules

import android.graphics.RectF
import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.ai.TrackObj
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.usecase.ai.ViolationThrottle
import com.sos.chakhaeng.domain.usecase.ai.geom.centerX
import com.sos.chakhaeng.domain.usecase.ai.geom.centerY
import com.sos.chakhaeng.domain.usecase.ai.geom.headRegionOf
import com.sos.chakhaeng.domain.usecase.ai.geom.iou01
import com.sos.chakhaeng.domain.usecase.ai.geom.overlapRatio
import javax.inject.Inject
import kotlin.math.min

data class NoHelmetConfig(
    val minPerson: Float = 0.40f,
    val minVehicle: Float = 0.45f,
    val minHelmet: Float = 0.50f,
    val minNoHelmet: Float = 0.55f,
    val pvIou: Float = 0.20f,
    val pvCenterBoost: Float = 0.65f,
    val headOverlap: Float = 0.30f,
)

class NoHelmetRule @Inject constructor(
    private val cfg: NoHelmetConfig,
    private val throttle: ViolationThrottle,
) : ViolationRule {

    override val name: String = "NoHelmet"

    override fun evaluate(detections: List<Detection>, tracks: List<TrackObj>): List<ViolationEvent> {
        fun isPerson(x: Detection) = x.label.equals("person", true) && x.score >= cfg.minPerson
        fun isVehicle(x: Detection) = listOf("kickboard", "motorcycle", "bicycle")
            .any { x.label.equals(it, true) } && x.score >= cfg.minVehicle
        fun isHelmet(x: Detection) = x.label.equals("helmet", true) && x.score >= cfg.minHelmet
        fun isNoHelmet(x: Detection) = x.label.equals("no-helmet", true) && x.score >= cfg.minNoHelmet

        val persons = detections.filter(::isPerson)
        val vehicles = detections.filter(::isVehicle)
        if (persons.isEmpty() || vehicles.isEmpty()) return emptyList()

        val helmets = detections.filter(::isHelmet)
        val noHelmets = detections.filter(::isNoHelmet)

        val events = mutableListOf<ViolationEvent>()

        // 사람-차량 페어링
        val pairs = mutableListOf<Pair<Detection, Detection>>()
        for (v in vehicles) {
            var bestP: Detection? = null
            var bestScore = 0f
            for (p in persons) {
                val i = iou01(v.box, p.box)
                val cInside = v.box.contains(centerX(p.box), centerY(p.box))
                val s = if (cInside) cfg.pvCenterBoost else i
                if (s > bestScore && (i >= cfg.pvIou || cInside)) {
                    bestScore = s
                    bestP = p
                }
            }
            if (bestP != null) pairs += bestP!! to v
        }

        for ((p, v) in pairs) {
            val head = headRegionOf(p.box, 0.35f)
            // A) 명시적 no-helmet
            val nh = noHelmets.firstOrNull { overlapRatio(head, it.box) >= cfg.headOverlap }
            if (nh != null) {
                val veh = if (listOf("kickboard","e-scooter","scooter").any { v.label.equals(it, true) }) "KICKBOARD" else "MOTORCYCLE"
                val conf = min(p.score, min(v.score, nh.score))
                val evt = ViolationEvent(type = "헬멧 미착용", confidence = conf)
                val region = union(p.box, v.box)
                if (throttle.allow(evt, region)) events += evt
                continue
            }
            // B) 헬멧 미겹침 → 의심
            val hasHelmetOnHead = helmets.any { overlapRatio(head, it.box) >= cfg.headOverlap }
            if (!hasHelmetOnHead) {
                val veh = if (listOf("kickboard","e-scooter","scooter").any { v.label.equals(it, true) }) "KICKBOARD" else "MOTORCYCLE"
                val conf = min(p.score, v.score) * 0.75f
                val evt = ViolationEvent(type = "헬멧 미착용", confidence = conf)
                val region = union(p.box, v.box)
                if (throttle.allow(evt, region)) events += evt
            }
        }
        return events
    }

    private fun union(a: RectF, b: RectF) = RectF(
        minOf(a.left, b.left),
        minOf(a.top, b.top),
        maxOf(a.right, b.right),
        maxOf(a.bottom, b.bottom),
    )

}