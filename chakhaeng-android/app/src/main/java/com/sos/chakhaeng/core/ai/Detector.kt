package com.sos.chakhaeng.core.ai

import android.graphics.Bitmap

interface Detector: AutoCloseable {
    suspend fun warmup()
    suspend fun detect(bitmap: Bitmap, rotation: Int = 0): Pair<List<Detection>, LaneDetection>
}