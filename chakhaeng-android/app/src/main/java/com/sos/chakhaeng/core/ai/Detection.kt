package com.sos.chakhaeng.core.ai

import android.graphics.RectF

data class Detection (
    val label: String,
    val score: Float,
    val box: RectF // 화면 크기 기준 정규화 좌표 (0f ~ 1f)
)

data class BBoxN( // 정규화 좌표 (0~1)
    val x: Float, val y: Float, val w: Float, val h: Float
) {
    val xCenter get() = x + w * 0.5f
    val yTop    get() = y
    val yBottom get() = y + h
    fun bottomCenter() = xCenter to yBottom
}

data class TrackObj(
    val id: Int,
    val label: String,
    val conf: Float,
    val box: BBoxN
)

data class DetObj( // YOLO 검출(신호/횡단보도 등)
    val label: String,
    val conf: Float,
    val box: BBoxN
)

data class ViolationEvent(
    val trackId: Int,
    val whenMs: Long,
    val vehicleLabel: String,
    val crosswalkTopY: Float,  // 정규화 y
    val bottomCenterY: Float   // 정규화 y
)

// model의 index -> name 매핑 (질문에 주신 클래스로 예시)
val LABELS = listOf(
    "bicycle","car","carplate","crosswalk","helmet","invisible_signal_None","kickboard","lovebug",
    "motorcycle","no-helmet","pedestrian_signal_etc","pedestrian_signal_green","pedestrian_signal_red",
    "person","unusual_signal_bus","vehicular_signal_etc","vehicular_signal_green",
    "vehicular_signal_green and green arrow","vehicular_signal_green and yellow","vehicular_signal_green arrow",
    "vehicular_signal_green arrow and green arrow","vehicular_signal_green arrow down",
    "vehicular_signal_red","vehicular_signal_red and green arrow","vehicular_signal_red and yellow","vehicular_signal_yellow"
)
