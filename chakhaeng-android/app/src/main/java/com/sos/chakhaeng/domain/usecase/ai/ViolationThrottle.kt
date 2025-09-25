// domain/usecase/ai/ViolationThrottle.kt
package com.sos.chakhaeng.domain.usecase.ai

import android.graphics.RectF
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.usecase.ai.geom.iou01

class ViolationThrottle(
    private val cooldownMs: Long = 10000L,
    private val dedupIou: Float = 0.50f,
) {
    private data class Seen(val whenMs: Long, val box: RectF, val type: String)
    private val seen = mutableListOf<Seen>()

    fun allow(evt: ViolationEvent, region: RectF, nowMs: Long = System.currentTimeMillis()): Boolean {
        seen.removeAll { nowMs - it.whenMs > cooldownMs }
        val found = seen.firstOrNull { iou01(it.box, region) >= dedupIou && it.type == evt.type }
        if (found != null && nowMs - found.whenMs < cooldownMs) return false
        seen += Seen(nowMs, region, evt.type)
        return true
    }
}
