package com.sos.chakhaeng.domain.usecase.ai

import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import javax.inject.Inject

class ProcessDetectionsUseCase @Inject constructor() {

    operator fun invoke(input: List<Detection>): List<ViolationEvent> {
        val out = mutableListOf<ViolationEvent>()

        val cars = input.filter { it.label.equals("cars", ignoreCase = true) }
        val crosswalks = input.filter { it.label.contains("crosswalk", ignoreCase = true) }

        for (c in cars) for (x in crosswalks) {
            val iou = iou(c.box, x.box)
            if (iou > 0.3f) out += ViolationEvent("CROSSWALK_INVADE", ((c.score + x.score)/2f))
        }

        return out

    }


    private fun iou(a: android.graphics.RectF, b: android.graphics.RectF): Float {
        val left = maxOf(a.left, b.left)
        val top = maxOf(a.top, b.top)
        val right = minOf(a.right, b.right)
        val bottom = minOf(a.bottom, b.bottom)
        val inter = maxOf(0f, right - left) * maxOf(0f, bottom - top)
        val union = a.width() * a.height() + b.width() * b.height() - inter
        return if (union <= 0f) 0f else inter / union
    }
}