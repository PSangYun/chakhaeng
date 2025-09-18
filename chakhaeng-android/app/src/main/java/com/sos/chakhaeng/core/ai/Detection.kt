package com.sos.chakhaeng.core.ai

import android.graphics.RectF

data class Detection (
    val label: String,
    val score: Float,
    val box: RectF // 화면 크기 기준 정규화 좌표 (0f ~ 1f)
)