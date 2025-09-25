package axip.ailia_tracker

data class AiliaTrackerSettings(
    val score_threshold: Float,
    val nms_threshold: Float,
    val track_threshold: Float,
    val track_buffer: Int,
    val match_threshold: Float,
)
