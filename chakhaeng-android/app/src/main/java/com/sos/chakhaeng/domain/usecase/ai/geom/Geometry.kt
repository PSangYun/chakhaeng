package com.sos.chakhaeng.domain.usecase.ai.geom

import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

// 정규화(0..1) 박스용 IoU — YoloV8Parser와 동일 로직을 공통화
fun iou01(a: RectF, b: RectF): Float {
    val xA = max(a.left, b.left)
    val yA = max(a.top, b.top)
    val xB = min(a.right, b.right)
    val yB = min(a.bottom, b.bottom)
    val w = (xB - xA).coerceAtLeast(0f)
    val h = (yB - yA).coerceAtLeast(0f)
    val inter = w * h
    if (inter <= 0f) return 0f
    val areaA = (a.right - a.left).coerceAtLeast(0f) * (a.bottom - a.top).coerceAtLeast(0f)
    val areaB = (b.right - b.left).coerceAtLeast(0f) * (b.bottom - b.top).coerceAtLeast(0f)
    if (areaA <= 0f || areaB <= 0f) return 0f
    return inter / (areaA + areaB - inter)
}

fun overlapRatio(a: RectF, b: RectF): Float {
    val xA = max(a.left, b.left)
    val yA = max(a.top, b.top)
    val xB = min(a.right, b.right)
    val yB = min(a.bottom, b.bottom)
    val inter = (xB - xA).coerceAtLeast(0f) * (yB - yA).coerceAtLeast(0f)
    val small = min(
        (a.right - a.left).coerceAtLeast(0f) * (a.bottom - a.top).coerceAtLeast(0f),
        (b.right - b.left).coerceAtLeast(0f) * (b.bottom - b.top).coerceAtLeast(0f)
    )
    return if (small <= 0f) 0f else inter / small
}

fun headRegionOf(person: RectF, headRatio: Float = 0.35f): RectF {
    val h = person.height()
    val headBottom = person.top + h * headRatio
    return RectF(person.left, person.top, person.right, headBottom)
}

fun centerX(r: RectF) = (r.left + r.right) / 2f
fun centerY(r: RectF) = (r.top + r.bottom) / 2f
