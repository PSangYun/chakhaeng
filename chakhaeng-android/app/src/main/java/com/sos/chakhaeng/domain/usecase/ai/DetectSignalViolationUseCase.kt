package com.sos.chakhaeng.domain.usecase.ai

import com.sos.chakhaeng.core.ai.*
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 실시간 프레임마다 호출:
 * - YOLO detections(픽셀 좌표) → 차량만 ByteTrack에 투입해 추적
 * - YOLO 전체를 정규화하여 SignalViolationDetection 정책에 투입
 * - 발생한 신호위반을 도메인 ViolationEvent 로 매핑
 *
 * 내부에 ByteTrack/SignalViolationDetection 상태를 들고 있어 '연속 프레임' 기준으로 동작합니다.
 */
@Singleton
class DetectSignalViolationUseCase @Inject constructor() {
    private val tracker = ByteTrackEngine(
        scoreThresh = 0.20f,
        nmsThresh   = 0.70f,
        trackThresh = 0.50f,
        trackBuffer = 45,
        matchThresh = 0.80f
    )

    private val signalLogic = SignalViolationDetection(
        vehicleLabels = setOf("car","motorcycle","bicycle","kickboard","lovebug"),
        crosswalkLabel = "crosswalk",
        vehicularSignalPrefix = "vehicular_signal_",
        crossingTol = 0.012f,
        lateralStepTol = 0.004f,
        lateralAccumThresh = 0.03f
    )

    private val labelToIndex by lazy {
        TrafficLabels.LABELS.withIndex().associate { it.value to it.index }
    }

    operator fun invoke(
        dets: List<Detection>,
        frameW: Int,
        frameH: Int,
        nowMs: Long = System.currentTimeMillis()
    ): List<ViolationEvent> {
        // 1) 차량만 ByteTrack 입력(픽셀좌표 → 내부에서 정규화)
        val btInputs: List<ByteTrackEngine.Det> = dets.mapNotNull { d ->
            val idx = labelToIndex[d.label] ?: return@mapNotNull null
            if (idx !in TrafficLabels.VEH_IDX) return@mapNotNull null
            ByteTrackEngine.Det(
                category = idx,
                conf = d.score,
                x = d.box.left, y = d.box.top,
                w = d.box.width(), h = d.box.height()
            )
        }

        val tracksRaw = tracker.update(btInputs)
        val trackObjs = tracksRaw.map { it.toTrackObj() }

        // 2) 정책 입력(정규화 좌표)
        val detObjs = dets.map { it.toNormalizedDetObj(frameW, frameH) }
        val hits     = signalLogic.updateAndDetectViolations(detObjs, trackObjs, nowMs)

        // 3) 도메인 이벤트로 매핑
        return hits.map {
            ViolationEvent(
                id = it.trackId.toString(),          // ✅ 트래킹 ID를 그대로 사용
                type = ViolationType.SIGNAL,
                detectedAt = it.whenMs
            )
        }
    }

    /** 서비스/세션 리셋 시 호출(선택) */
    fun reset() {
        // 필요 시 내부 상태 초기화가 필요하면 SignalViolationDetection에 reset() 만들어 호출
        // 현재 구현은 phase 전환으로 충분하면 비워도 OK
    }

    fun close() {
        tracker.close()
    }
}
