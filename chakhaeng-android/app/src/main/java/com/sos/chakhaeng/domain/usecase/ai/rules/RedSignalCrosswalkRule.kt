// domain/usecase/ai/rules/RedSignalCrosswalkRule.kt
package com.sos.chakhaeng.domain.usecase.ai.rules

import android.graphics.RectF
import com.sos.chakhaeng.core.ai.*
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.usecase.ai.ViolationThrottle
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/** 룰 파라미터 (원하면 RemoteConfig/Datastore로 교체) */
data class RedSignalConfig(
    val vehicleLabels: Set<String> = setOf("car","motorcycle","bicycle","kickboard","lovebug"),
    val crosswalkLabels: Set<String> = setOf("crosswalk"),
    val vehicularSignalPrefix: String = "vehicular_signal_",

    val crossingTol: Float = 0.012f,       // 위/아래 밴드 판정 여유(y 정규화)
    val lateralStepTol: Float = 0.004f,    // 프레임간 최소 왼쪽 이동량
    val lateralAccumThresh: Float = 0.03f, // 누적 왼쪽 이동 임계
    val crosswalkIouThresh: Float = 0.02f  // 교차로 IoU 임계
)

/**
 * 빨간불 시 횡단보도 침입/진입 방향성 검사
 * - ByteTrack 엔진을 내부에서 운용(정규화 [0,1] LTRB 기준)
 * - 룰 인터페이스는 그대로: evaluate(detections)만 받음. (다른 룰들과 동일) :contentReference[oaicite:0]{index=0}
 */
class RedSignalCrosswalkRule @Inject constructor(
    private val cfg: RedSignalConfig,
    private val throttle: ViolationThrottle
) : ViolationRule {

    override val name: String = "RedSignalCrosswalk"

    // label → index (ByteTrack 카테고리)
    private val labelToIndex by lazy {
        TrafficLabels.LABELS.withIndex().associate { it.value.trim().lowercase() to it.index }
    }

    // ByteTrack (정규화 좌표로 사용)
    private val tracker = ByteTrackEngine(
        scoreThresh = 0.10f,
        nmsThresh   = 0.70f,
        trackThresh = 0.25f,
        trackBuffer = 90,
        matchThresh = 0.70f
    )

    // 프레임 간 상태
    private val prevBottomY = mutableMapOf<Int, Float>()
    private val prevCenterX = mutableMapOf<Int, Float>()
    private val accumLeftDx = mutableMapOf<Int, Float>() // 왼쪽(음수) 누적
    private val recordedInThisPhase = mutableSetOf<Int>()
    private var isRed = false
    private var phaseId = 0

    override fun evaluate(detections: List<Detection>): List<ViolationEvent> {
        if (detections.isEmpty()) return emptyList()

        // 1) 신호 선택: 화면에서 가장 위(= y 최소)인 vehicular_signal_* 1개
        val vehSignals = detections.filter { it.label.startsWith(cfg.vehicularSignalPrefix, ignoreCase = true) }
        val primarySignal = vehSignals.minByOrNull { it.box.top }
        val redNow = primarySignal?.label?.contains("red", ignoreCase = true) == true

        // 2) 횡단보도 선택: 화면에서 가장 아래쪽(= top 최대)
        val crosswalk = detections
            .filter { cfg.crosswalkLabels.any { lbl -> it.label.equals(lbl, true) } }
            .maxByOrNull { it.box.top }
        val cwRect = crosswalk?.box        // 정규화 LTRB로 가정

        // phase 전환 시 누적 초기화
        if (redNow && !isRed) {
            phaseId++
            recordedInThisPhase.clear()
            accumLeftDx.clear()
        }
        isRed = redNow

        // 3) 차량만 추려서 ByteTrack 입력 (정규화 LTRB -> N(x,y,w,h))
        val vehIdxSet = TrafficLabels.VEH_IDX
        val vehForTrack = detections.mapNotNull { d ->
            val idx = labelToIndex[d.label.trim().lowercase()] ?: return@mapNotNull null
            if (idx !in vehIdxSet) return@mapNotNull null
            val l = d.box.left; val t = d.box.top; val r = d.box.right; val b = d.box.bottom
            val w = (r - l).coerceAtLeast(1e-6f)
            val h = (b - t).coerceAtLeast(1e-6f)
            ByteTrackEngine.Det(category = idx, conf = d.score, x = l, y = t, w = w, h = h)
        }

        val tracks = tracker.update(vehForTrack).map { it.toTrackObj() } // Track → TrackObj (정규화 반환) :contentReference[oaicite:1]{index=1}
        if (!isRed || cwRect == null) {
            // 녹색 or 횡단보도 없음 → 위치만 갱신
            tracks.forEach { t ->
                prevBottomY[t.id] = t.box.yBottom
                prevCenterX[t.id] = t.box.xCenter
            }
            gcByAlive(tracks)
            return emptyList()
        }

        // 4) 빨간불 + 횡단보도 있을 때만 위반 판정
        val topY = cwRect.top
        val bottomY = cwRect.bottom
        val events = mutableListOf<ViolationEvent>()

        for (t in tracks) {
            val currCx = t.box.xCenter
            val currBottomY = t.box.yBottom
            val prevY = prevBottomY[t.id]
            val prevX = prevCenterX[t.id]

            // 밴드/IoU 안에 있는지
            val insideBandByY = currBottomY <= bottomY + cfg.crossingTol && currBottomY >= topY - cfg.crossingTol
            val insideByIou = iou(toRectF(t.box), cwRect) >= cfg.crosswalkIouThresh
            val inside = insideBandByY || insideByIou

            // (A) 윗선 통과(아래→위) : prevY >= top && curr < top
            val crossedUp = prevY != null && prevY >= topY - cfg.crossingTol && currBottomY < topY - cfg.crossingTol

            // (B) 횡단보도 위에서 왼쪽 진행 누적
            var makeEvent = false
            if (crossedUp && t.id !in recordedInThisPhase) {
                makeEvent = true
            } else if (inside && prevX != null) {
                val stepDx = currCx - prevX
                if (stepDx <= -cfg.lateralStepTol) {
                    val acc = (accumLeftDx[t.id] ?: 0f) + stepDx
                    accumLeftDx[t.id] = acc
                    if (acc <= -cfg.lateralAccumThresh && t.id !in recordedInThisPhase) {
                        makeEvent = true
                        accumLeftDx[t.id] = 0f
                    }
                } else if (stepDx > 0f) {
                    // 오른쪽으로 되돌아가면 누적 초기화
                    accumLeftDx.remove(t.id)
                }
            } else {
                accumLeftDx.remove(t.id)
            }

            if (makeEvent) {
                val evt = ViolationEvent(type = "신호위반", confidence = (primarySignal?.score ?: 0.9f))
                val region = union(toRectF(t.box), cwRect)
                if (throttle.allow(evt, region)) events += evt
                recordedInThisPhase += t.id
            }

            // 상태 갱신
            prevBottomY[t.id] = currBottomY
            prevCenterX[t.id] = currCx
        }

        gcByAlive(tracks)
        return events
    }

    // ---------- helpers ----------
    private fun gcByAlive(tracks: List<TrackObj>) {
        val alive = tracks.map { it.id }.toSet()
        prevBottomY.keys.retainAll(alive)
        prevCenterX.keys.retainAll(alive)
        accumLeftDx.keys.retainAll(alive)
    }

    private fun toRectF(b: BBoxN) = RectF(b.x, b.y, b.x + b.w, b.y + b.h)

    private fun union(a: RectF, b: RectF) = RectF(
        min(a.left, b.left),
        min(a.top, b.top),
        max(a.right, b.right),
        max(a.bottom, b.bottom)
    )

    private fun iou(a: RectF, b: RectF): Float {
        val x1 = max(a.left, b.left)
        val y1 = max(a.top, b.top)
        val x2 = min(a.right, b.right)
        val y2 = min(a.bottom, b.bottom)
        val inter = max(0f, x2 - x1) * max(0f, y2 - y1)
        val ua = (a.right - a.left) * (a.bottom - a.top)
        val ub = (b.right - b.left) * (b.bottom - b.top)
        return inter / (ua + ub - inter + 1e-6f)
    }
}
