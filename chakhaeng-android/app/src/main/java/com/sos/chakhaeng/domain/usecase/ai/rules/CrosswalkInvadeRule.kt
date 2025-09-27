package com.sos.chakhaeng.domain.usecase.ai.rules

import android.graphics.RectF
import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.ai.TrackObj
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.usecase.ai.ViolationThrottle
import com.sos.chakhaeng.domain.usecase.ai.geom.iou01
import javax.inject.Inject
import kotlin.math.min

data class CrosswalkConfig(
    // ✅ 라벨 테이블(오타 수정 + 확장 가능)
    val motoLabels: List<String> = listOf("motocycle", "car", "bicycle"),
    val crosswalkLabels: List<String> = listOf("crosswalk", "zebra_crossing"),
    val redSignalLabels: List<String> = listOf("vehicular_signal_red"),

    // 점수 컷오프
    val minMotoScore: Float = 0.40f,
    val minCrosswalkScore: Float = 0.35f,
    val minRedSignalScore: Float = 0.45f,

    // 매칭 기준
    val invadeIou: Float = 0.20f, // 조금 더 관대하게(0.30→0.20)
)

class CrosswalkInvadeRule @Inject constructor(
    private val cfg: CrosswalkConfig,
    private val throttle: ViolationThrottle,   // ✅ NoHelmetRule처럼 throttle 사용
) : ViolationRule {

    override val name: String = "CrosswalkInvade"

    override fun evaluate(
        detections: List<Detection>,
        tracks: List<TrackObj>
    ): List<ViolationEvent> {
        fun isMoto(x: Detection) =
            cfg.motoLabels.any { x.label.equals(it, true) } && x.score >= cfg.minMotoScore

        fun isCrosswalk(x: Detection) =
            cfg.crosswalkLabels.any { x.label.equals(it, true) || x.label.contains(it, true) } &&
                    x.score >= cfg.minCrosswalkScore

        fun isRedSignal(x: Detection) =
            cfg.redSignalLabels.any { x.label.equals(it, true) } && x.score >= cfg.minRedSignalScore

        val motos = detections.filter(::isMoto)
        val crosswalks = detections.filter(::isCrosswalk)
        val reds = detections.filter(::isRedSignal)

        if (motos.isEmpty() || crosswalks.isEmpty() || reds.isEmpty()) return emptyList()

        val bestRed = reds.maxBy { it.score }
        val out = mutableListOf<ViolationEvent>()

        for (m in motos) for (cw in crosswalks) {
            val centerInside = cw.box.contains(m.box.centerX(), m.box.centerY())
            if (iou01(m.box, cw.box) >= cfg.invadeIou || centerInside) {
                val conf = min(min(m.score, cw.score), bestRed.score)
                val evt = ViolationEvent(
                    type = "신호위반",
                    confidence = conf,
                    // 시연 요구: 트리거 후 TTS 5초 지연
                    attrs = mapOf("ttsDelayMs" to 10_000L)
                )
                val region = union(m.box, cw.box)
                if (throttle.allow(evt, region)) out += evt
            }
        }
        return out
    }

    private fun union(a: RectF, b: RectF) = RectF(
        minOf(a.left, b.left), minOf(a.top, b.top),
        maxOf(a.right, b.right), maxOf(a.bottom, b.bottom)
    )
}
