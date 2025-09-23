package com.sos.chakhaeng.core.ai

import android.util.Log

class SignalViolationDetection(private val vehicleLabels: Set<String> = setOf("car","motorcycle","bicycle","kickboard","lovebug"),
                               private val crosswalkLabel: String = "crosswalk",
                               private val vehicularSignalPrefix: String = "vehicular_signal_",
                               private val crossingTol: Float = 0.01f,   // y축 잡음 허용 (정규화 기준, ≈ 1% 높이)
                               private val lateralStepTol: Float = 0.004f, // 프레임 간 최소 왼쪽 이동량(정규화)
                               private val lateralAccumThresh: Float = 0.03f // 횡단보도 위에서 왼쪽 누적 이동 임계(정규화)
) {
    // 상태: 이전 프레임의 차량 바닥 y, 현재 red phase에서 이미 기록한 차량
    private val prevBottomY = mutableMapOf<Int, Float>()
    private val prevCenterX = mutableMapOf<Int, Float>()
    private val accumLeftDx = mutableMapOf<Int, Float>() // 음수(왼쪽)로 누적
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
        val crosswalk = detections.filter { it.label == crosswalkLabel }
            .maxByOrNull { it.box.yTop }
        val cw = crosswalk?.box
        val crosswalkTopY = cw?.yTop
        val crosswalkBottomY = cw?.let { it.yTop + it.h }

        // phase 관리: 빨간불 진입 시 위반 중복 카운트 초기화
        if (redNow && !isRed) { phaseId++; recordedInThisPhase.clear(); accumLeftDx.clear() }
        isRed = redNow

        // 4) 차량 트랙만 선별
        val vehicleTracks = tracks.filter { it.label in vehicleLabels }
        val violations = mutableListOf<ViolationEvent>()

        if (isRed && crosswalkTopY != null) {
            val topY = cw.yTop
            val bottomY = cw.yTop + cw.h
            for (t in vehicleTracks) {
                val (currCx, currBottomY) = t.box.bottomCenter()
                val prevY = prevBottomY[t.id]
                val prevX = prevCenterX[t.id]

                /// -----------------------------
                // A) 세로 경계선 교차(기존 규칙)
                // -----------------------------
                val crossedUp =
                    prevY != null &&
                            prevY >= crosswalkTopY - crossingTol &&
                            currBottomY <  crosswalkTopY - crossingTol

                if (crossedUp && t.id !in recordedInThisPhase) {
                    Log.w(
                        "SignalViolation",
                        "RED-CROSS id=${t.id} label=${t.label} ts=$nowMs " +
                                "prevY=${"%.3f".format(prevY!!)} currY=${"%.3f".format(currBottomY)} topY=${"%.3f".format(crosswalkTopY)}"
                    )
                    violations += ViolationEvent(
                        // TODO: 네 프로젝트의 ViolationEvent 필드에 맞게 세팅
                        trackId = t.id,
                        whenMs = nowMs,
                        vehicleLabel = t.label,
                        crosswalkTopY = crosswalkTopY,
                        bottomCenterY = currBottomY
                    )
                    recordedInThisPhase += t.id
                }

                // --------------------------------------------
                // B) 횡단보도 "영역 안"에서 왼쪽으로 쭉 이동(신규 규칙)
                // --------------------------------------------
                val insideCrosswalkBand =
                    currBottomY <= bottomY + crossingTol &&
                            currBottomY >= topY - crossingTol

                if (insideCrosswalkBand && prevX != null) {
                    val stepDx = currCx - prevX // 왼쪽 이동이면 stepDx < 0
                    if (stepDx <= -lateralStepTol) {
                        val acc = (accumLeftDx[t.id] ?: 0f) + stepDx // 음수 누적
                        accumLeftDx[t.id] = acc
                        if (acc <= -lateralAccumThresh && t.id !in recordedInThisPhase) {
                            Log.w(
                                "SignalViolation",
                                "RED-MOVE-LEFT id=${t.id} label=${t.label} ts=$nowMs " +
                                        "accLeftDx=${"%.3f".format(acc)} stepDx=${"%.3f".format(stepDx)} " +
                                        "band=[${"%.3f".format(crosswalkTopY)}..${"%.3f".format(crosswalkBottomY)}] y=${"%.3f".format(currBottomY)}"
                            )
                            violations += ViolationEvent(
                                // TODO: 프로젝트의 ViolationEvent 필드에 맞게 세팅
                                trackId = t.id,
                                whenMs = nowMs,
                                vehicleLabel = t.label,
                                crosswalkTopY = crosswalkTopY,
                                bottomCenterY = currBottomY
                            )
                            recordedInThisPhase += t.id
                            // 같은 phase에서 같은 차량의 중복 트리거 방지
                            accumLeftDx[t.id] = 0f
                        }
                    } else if (stepDx > 0f) {
                        // 오른쪽 이동/정지면 누적을 서서히 해제(또는 즉시 리셋)
                        accumLeftDx.remove(t.id)
                    }
                } else {
                    // 횡단보도 영역 밖이면 누적 리셋
                    accumLeftDx.remove(t.id)
                }

                // 상태 갱신
                prevBottomY[t.id] = currBottomY
                prevCenterX[t.id] = currCx
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