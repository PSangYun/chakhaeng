package com.sos.chakhaeng.core.ai

class SignalViolationDetection(private val vehicleLabels: Set<String> = setOf("car","motorcycle","bicycle","kickboard","lovebug"),
                               private val crosswalkLabel: String = "crosswalk",
                               private val vehicularSignalPrefix: String = "vehicular_signal_",
                               private val crossingTol: Float = 0.01f   // y축 잡음 허용 (정규화 기준, ≈ 1% 높이)
) {
    // 상태: 이전 프레임의 차량 바닥 y, 현재 red phase에서 이미 기록한 차량
    private val prevBottomY = mutableMapOf<Int, Float>()
    private var isRed = false
    private var phaseId = 0
    private val recordedInThisPhase = mutableSetOf<Int>()

    /** 프레임별로 호출: 검출(신호/횡단보도) + 트랙(차량)으로 위반 이벤트를 계산 */
    fun updateAndDetectViolations(
        detections: List<DetObj>,    // YOLO 원시 검출들
        tracks: List<TrackObj>,      // ByteTrack 결과(차량)
        nowMs: Long = System.currentTimeMillis()
    ): List<ViolationEvent> {
        // 1) 신호 선택: 화면 "가장 위" (yTop 최소)인 vehicular_signal_* 1개
        val vehSignals = detections.filter { it.label.startsWith(vehicularSignalPrefix) }
        val primarySignal = vehSignals.minByOrNull { it.box.yTop }

        // 2) red 판정 (정책에 맞게 조정 가능)
        val redNow = primarySignal?.let { isRedSignal(it.label) } ?: false

        // 3) 횡단보도 선택: 카메라에 가장 가까워 보이는(= 화면 아래쪽) 횡단보도 1개
        val crosswalks = detections.filter { it.label == crosswalkLabel }
        val targetCrosswalk = crosswalks.maxByOrNull { it.box.yTop }  // y가 클수록 아래에 있음
        val crosswalkTopY = targetCrosswalk?.box?.yTop

        // phase 관리: 빨간불 진입 시 위반 중복 카운트 초기화
        if (redNow && !isRed) { phaseId++; recordedInThisPhase.clear() }
        isRed = redNow

        // 4) 차량 트랙만 선별
        val vehicleTracks = tracks.filter { it.label in vehicleLabels }

        val violations = mutableListOf<ViolationEvent>()

        if (isRed && crosswalkTopY != null) {
            for (t in vehicleTracks) {
                val (_, currBottomY) = t.box.bottomCenter()
                val prevY = prevBottomY[t.id]

                // "아래(=prev >= topY)" → "위(=curr < topY - tol)" 로 처음 넘어가면 위반
                if (prevY != null &&
                    prevY >= crosswalkTopY - crossingTol &&
                    currBottomY < crosswalkTopY - crossingTol &&
                    t.id !in recordedInThisPhase
                ) {
                    violations += ViolationEvent(
                        trackId = t.id,
                        whenMs = nowMs,
                        vehicleLabel = t.label,
                        crosswalkTopY = crosswalkTopY,
                        bottomCenterY = currBottomY
                    )
                    recordedInThisPhase += t.id
                }

                // 상태 갱신
                prevBottomY[t.id] = currBottomY
            }
        } else {
            // 빨간불이 아니면 위치만 갱신하고, 이전 phase 기록은 유지
            for (t in vehicleTracks) {
                prevBottomY[t.id] = t.box.bottomCenter().second
            }
        }

        // 사라진 트랙 정리(유지 비용 줄이기)
        val aliveIds = vehicleTracks.map { it.id }.toSet()
        prevBottomY.keys.retainAll(aliveIds)

        return violations
    }

    /** ‘빨간불’ 정책: 단순 버전(문자열에 red 포함) */
    private fun isRedSignal(label: String): Boolean {
        // 예: "vehicular_signal_red", "vehicular_signal_red and yellow", "vehicular_signal_red and green arrow"
        // ※ 직진/좌회전 구분이 필요하면 여기서 'green arrow' 예외 처리
        return label.startsWith(vehicularSignalPrefix) && label.contains("red")
    }
}