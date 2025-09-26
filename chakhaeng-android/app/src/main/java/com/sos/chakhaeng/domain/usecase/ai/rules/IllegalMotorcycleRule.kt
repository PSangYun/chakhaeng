package com.sos.chakhaeng.domain.usecase.ai.rules

import android.graphics.RectF
import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.ai.TrackObj
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.usecase.ai.ViolationThrottle
import javax.inject.Inject

data class IllegalMotorcycleConfig(
    val minIllegalScore: Float = 0.50f, // "ileegal_motorcycle" 최소 신뢰도
    val minBoxArea: Float = 0.002f      // 너무 작은 박스(원거리/노이즈) 컷, 정규화 기준
)

class IllegalMotorcycleRule @Inject constructor(
    private val cfg: IllegalMotorcycleConfig,
    private val throttle: ViolationThrottle
) : ViolationRule {

    override val name: String = "IllegalMotorcycle"

    // LovebugRule 과 동일한 시그니처로 맞춤
    override fun evaluate(
        detections: List<Detection>,
        tracks: List<TrackObj>
    ): List<ViolationEvent> {
        fun isIllegalMotorcycle(d: Detection): Boolean =
            d.score >= cfg.minIllegalScore &&
                    d.label.equals("ileegal_motorcycle", ignoreCase = true)

        val illegals = detections.filter(::isIllegalMotorcycle)
        if (illegals.isEmpty()) return emptyList()

        val out = mutableListOf<ViolationEvent>()
        for (d in illegals) {
            // 너무 작은 박스(정규화 좌표 가정) 제거
            val area = (d.box.right - d.box.left).coerceAtLeast(0f) *
                    (d.box.bottom - d.box.top).coerceAtLeast(0f)
            if (area < cfg.minBoxArea) continue

            val evt = ViolationEvent(
                type = "무번호판",
                confidence = d.score
            )
            val region: RectF = d.box // 해당 객체 박스를 그대로 지역으로 사용

            if (throttle.allow(evt, region)) {
                out += evt
            }
        }
        return out
    }
}
