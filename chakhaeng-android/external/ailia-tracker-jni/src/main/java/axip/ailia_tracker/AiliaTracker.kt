package axip.ailia_tracker

import android.util.Log

class AiliaTracker(
    algorithm: Int,
    settings: AiliaTrackerSettings
) {
    companion object {
        const val AILIA_TRACKER_FLAG_NONE = 0
        const val AILIA_TRACKER_ALGORITHM_BYTE_TRACK = 0

        init {
            System.loadLibrary("ailia_tracker")
        }
    }

    private val tag = AiliaTracker::class.simpleName
    private var tracker: Long = 0

    init {
        tracker = create(algorithm, settings)
        if (tracker == 0L) {
            Log.e(tag, "Failed to create tracker")
        }
    }

    fun addTarget(category: Int, prob: Float, x: Float, y: Float, w: Float, h: Float): Int {
        return addTarget(tracker, category, prob, x, y, w, h)
    }

    fun compute(): Int {
        return compute(tracker)
    }

    fun getObjectCount(): Int {
        return getObjectCount(tracker)
    }

    fun getObject(index: Int): AiliaTrackerObject? {
        var obj : AiliaTrackerObject = AiliaTrackerObject(0, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
        getObject(tracker, index, obj)
        return obj;
    }

    fun getErrorDetail(): String? {
        return getErrorDetail(tracker)
    }

    fun close() {
        destroy(tracker)
    }

    private external fun create(algorithm: Int, settings: AiliaTrackerSettings): Long
    private external fun destroy(tracker: Long)
    private external fun addTarget(tracker: Long, category: Int, prob: Float, x: Float, y: Float, w: Float, h: Float): Int
    private external fun compute(tracker: Long): Int
    private external fun getObjectCount(tracker: Long): Int
    private external fun getObject(tracker: Long, index: Int, obj: AiliaTrackerObject)
    private external fun getErrorDetail(tracker: Long): String?
}
