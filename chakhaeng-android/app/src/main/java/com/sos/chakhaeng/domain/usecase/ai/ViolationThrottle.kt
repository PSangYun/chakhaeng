// domain/usecase/ai/ViolationThrottle.kt
package com.sos.chakhaeng.domain.usecase.ai

import android.graphics.RectF
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.usecase.ai.geom.iou01
import javax.inject.Inject

data class ViolationThrottleConfig(
    val defaultCooldownMs: Long = 1_000L,
    val dedupIou: Float = 0.30f,
    val perTypeCooldownMs: Map<String, Long> = emptyMap(),
    val globalCooldownTypes: Set<String> = emptySet(),
)

class ViolationThrottle @Inject constructor(
    private val cfg: ViolationThrottleConfig,
) {
    private data class Seen(val whenMs: Long, val box: RectF, val type: String)
    private val seen = mutableListOf<Seen>()

    private fun cooldownFor(type: String) =
        (cfg.perTypeCooldownMs[type] ?: cfg.defaultCooldownMs)

    fun allow(evt: ViolationEvent, region: RectF, nowMs: Long = System.currentTimeMillis()): Boolean {
        seen.removeAll { nowMs - it.whenMs > cooldownFor(it.type) }

        val cd = cooldownFor(evt.type)
        val found = if (evt.type in cfg.globalCooldownTypes) {
            seen.firstOrNull { it.type == evt.type }
        } else {
            seen.firstOrNull { iou01(it.box, region) >= cfg.dedupIou && it.type == evt.type }
        }
        if (found != null && nowMs - found.whenMs < cd) return false
        seen += Seen(nowMs, region, evt.type)
        return true
    }
}
