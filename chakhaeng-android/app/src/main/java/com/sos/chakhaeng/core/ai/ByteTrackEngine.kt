package com.sos.chakhaeng.core.ai

import axip.ailia_tracker.*

class ByteTrackEngine(
    scoreThresh: Float = 0.2f,
    nmsThresh: Float = 0.7f,
    trackThresh: Float = 0.5f,
    trackBuffer: Int = 30,
    matchThresh: Float = 0.8f
) {
    private var tracker: AiliaTracker? = AiliaTracker(
        algorithm = AiliaTracker.AILIA_TRACKER_ALGORITHM_BYTE_TRACK,
        settings = AiliaTrackerSettings(
            score_threshold = scoreThresh,
            nms_threshold = nmsThresh,
            track_threshold = trackThresh,
            track_buffer = trackBuffer,
            match_threshold = matchThresh
        )
    )

    fun close() { runCatching { tracker?.close() }; tracker = null }

    data class Det(val category: Int, val conf: Float, val x: Float, val y: Float, val w: Float, val h: Float)
    data class Track(val id: Int, val category: Int, val conf: Float, val x: Float, val y: Float, val w: Float, val h: Float)

    /** 픽셀 좌표 입력도 허용. normW/H 주면 내부에서 [0,1]로 변환 */
    fun update(dets: List<Det>, normW: Float? = null, normH: Float? = null): List<Track> {
        val tk = tracker ?: return emptyList()
        for (d in dets) {
            val (nx, ny, nw, nh) = if (normW != null && normH != null)
                floatArrayOf(d.x / normW, d.y / normH, d.w / normW, d.h / normH)
            else floatArrayOf(d.x, d.y, d.w, d.h)

            tk.addTarget(d.category, d.conf, nx, ny, nw, nh)
        }
        val rc = tk.compute()
        if (rc != 0) return emptyList()

        val n = tk.getObjectCount()
        return (0 until n).mapNotNull { i ->
            tk.getObject(i)?.let { o ->
                Track(o.id, o.category, o.prob, o.x, o.y, o.w, o.h)
            }
        }
    }
}
