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

    /**
     * dets: [x,y,w,h]가 "정규화(0~1)"거나 "픽셀"일 수 있음.
     * 픽셀이라면 normW/H를 넘겨주세요(내부에서 0~1로 변환).
     */
    fun update(dets: List<Det>, normW: Float? = null, normH: Float? = null): List<Track> {
        val tk = tracker ?: return emptyList()

        var added = 0
        dets.forEachIndexed { idx, d ->
            val (nx, ny, nw, nh) = if (normW != null && normH != null)
                floatArrayOf(d.x / normW, d.y / normH, d.w / normW, d.h / normH)
            else floatArrayOf(d.x, d.y, d.w, d.h)

            // 안전 클램프 & 기초 검증
            val cx = nx.coerceIn(0f, 1f)
            val cy = ny.coerceIn(0f, 1f)
            val cw = nw.coerceAtLeast(0f)
            val ch = nh.coerceAtLeast(0f)

            val invalid =
                cw < 1e-4f || ch < 1e-4f || cx + cw > 1f + 1e-6f || cy + ch > 1f + 1e-6f

            if (invalid) {
                if (idx < 10) android.util.Log.d(
                    "BT.Engine",
                    "skip det idx=$idx raw=[${nx},${ny},${nw},${nh}] clamped=[${cx},${cy},${cw},${ch}]"
                )
                return@forEachIndexed
            }

            if (idx < 10) android.util.Log.d(
                "BT.Engine",
                "addTarget idx=$idx c=${d.category} conf=${"%.2f".format(d.conf)} N=[${"%.3f".format(cx)},${"%.3f".format(cy)},${"%.3f".format(cw)},${"%.3f".format(ch)}]"
            )

            tk.addTarget(d.category, d.conf, cx, cy, cw, ch)
            added++
        }

        val rc = tk.compute()
        if (rc != 0) {
            android.util.Log.w("BT.Engine", "compute rc=$rc added=$added")
            return emptyList()
        }

        val n = tk.getObjectCount()
        android.util.Log.d("BT.Engine", "compute ok added=$added tracks=$n")

        return (0 until n).mapNotNull { i ->
            tk.getObject(i)?.let { o ->
                Track(o.id, o.category, o.prob, o.x, o.y, o.w, o.h)
            }
        }
    }
}
