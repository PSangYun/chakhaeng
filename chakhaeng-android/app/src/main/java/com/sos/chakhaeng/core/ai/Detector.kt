package com.sos.chakhaeng.core.ai

import android.graphics.Bitmap

interface Detector: AutoCloseable {
    fun warmup()
    fun detect(bitmap: Bitmap, rotation: Int = 0): List<Detection>
}