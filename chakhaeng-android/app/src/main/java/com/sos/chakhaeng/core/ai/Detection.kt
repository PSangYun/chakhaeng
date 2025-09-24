package com.sos.chakhaeng.core.ai

import android.graphics.RectF

data class Detection (
    val label: String,
    val score: Float,
    val box: RectF, // 화면 크기 기준 정규화 좌표 (0f ~ 1f)
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
