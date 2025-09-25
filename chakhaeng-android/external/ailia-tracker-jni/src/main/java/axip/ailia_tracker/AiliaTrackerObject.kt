package axip.ailia_tracker

data class AiliaTrackerObject(
    val id: Int,
    val category: Int,
    val prob: Float,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float
)
