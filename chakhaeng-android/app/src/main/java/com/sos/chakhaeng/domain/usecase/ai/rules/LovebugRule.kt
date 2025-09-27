package com.sos.chakhaeng.domain.usecase.ai.rules

import android.graphics.RectF
import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.ai.TrackObj
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.usecase.ai.ViolationThrottle
import com.sos.chakhaeng.domain.usecase.ai.geom.centerX
import com.sos.chakhaeng.domain.usecase.ai.geom.centerY
import com.sos.chakhaeng.domain.usecase.ai.geom.iou01
import javax.inject.Inject
import kotlin.math.min

data class LovebugConfig(
    val minKickboard: Float = 0.45f,
    val minLovebug: Float = 0.40f,

    val kbLbIou: Float = 0.15f, // 킥보드-러브버그 IoU 임계값
    val centerInsideBoost: Float = 0.70f // 러브버그 중심이 킥보드 박스 안이면 강한 매칭
)

class LovebugRule @Inject constructor(
    private val cfg: LovebugConfig,
    private val throttle: ViolationThrottle
) : ViolationRule {

    override val name: String = "Lovebug"

    override fun evaluate(detections: List<Detection>, tracks: List<TrackObj>): List<ViolationEvent> {
        fun isKickboard(d: Detection) =
            d.score >= cfg.minKickboard && d.label.equals("kickboard", ignoreCase = true)
        fun isLovebug(d: Detection) =
            d.score >= cfg.minLovebug && d.label.equals("lovebug", ignoreCase = true)

        val kickboards = detections.filter(::isKickboard)
        val lovebugs = detections.filter(::isLovebug)
        if (kickboards.isEmpty() || lovebugs.isEmpty()) return emptyList()

        val events = mutableListOf<ViolationEvent>()

        // 러브버그 마다 가장 가까운 킥보드에 붙임
        for (lb in lovebugs) {
            var bestKb: Detection? = null
            var bestScore = 0f

            for (kb in kickboards) {
                val i = iou01(lb.box, kb.box)
                val centerInside = lb.box.contains(centerX(kb.box), centerY(kb.box))
                val s = if (centerInside) cfg.centerInsideBoost else i

                if (s > bestScore && (i >= cfg.kbLbIou || centerInside)) {
                    bestScore = s
                    bestKb = kb
                }
            }

            // 매칭 성공 시 위반 이벤트 생성
            if (bestKb != null) {
                val kb = bestKb!!
                val conf = min(kb.score, lb.score)
                val evt = ViolationEvent(
                    type = "킥보드 2인이상·헬멧 미착용",
                    confidence = conf,
                    announceTypes = listOf("킥보드 2인이상", "헬멧 미착용"))
                val region = union(kb.box, lb.box)
                if (throttle.allow(evt, region)) events += evt
            }
        }

        return events
    }

    private fun union(a: RectF, b: RectF) = RectF(
        minOf(a.left, b.left),
        minOf(a.top, b.top),
        maxOf(a.right, b.right),
        maxOf(a.bottom, b.bottom)
    )
}