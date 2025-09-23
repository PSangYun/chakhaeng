package com.sos.chakhaeng.core.ai

// model의 index -> name 매핑 (질문에 주신 클래스로 예시)
object TrafficLabels {
    val LABELS = listOf(
        "bicycle","car","carplate","crosswalk","helmet","invisible_signal_None","kickboard","lovebug",
        "motorcycle","no-helmet","pedestrian_signal_etc","pedestrian_signal_green","pedestrian_signal_red",
        "person","unusual_signal_bus","vehicular_signal_etc","vehicular_signal_green",
        "vehicular_signal_green and green arrow","vehicular_signal_green and yellow","vehicular_signal_green arrow",
        "vehicular_signal_green arrow and green arrow","vehicular_signal_green arrow down",
        "vehicular_signal_red","vehicular_signal_red and green arrow","vehicular_signal_red and yellow","vehicular_signal_yellow"
    )

    val VEH_IDX: Set<Int> = setOf(
        LABELS.indexOf("car"),
        LABELS.indexOf("motorcycle"),
        LABELS.indexOf("bicycle"),
        LABELS.indexOf("kickboard"),
        LABELS.indexOf("lovebug"),
    ).filter { it >= 0 }.toSet()
}

val Detection.x: Float get() = box.left
val Detection.y: Float get() = box.top
val Detection.w: Float get() = box.width()
val Detection.h: Float get() = box.height()

fun Detection.toNormalizedDetObj(frameW: Int, frameH: Int): DetObj {
    val nx = x / frameW
    val ny = y / frameH
    val nw = w / frameW
    val nh = h / frameH
    return DetObj(label = label, conf = score, box = BBoxN(nx, ny, nw, nh))
}

fun ByteTrackEngine.Track.toTrackObj(): TrackObj =
    TrackObj(
        id = id,
        label = TrafficLabels.LABELS.getOrNull(category) ?: category.toString(),
        conf = conf,
        box = BBoxN(x, y, w, h) // 이미 [0,1] 정규화로 돌아옴
    )

data class TrafficFrameResult(
    val detections: List<Detection>,               // YOLO 결과 (bbox=픽셀 좌표)
    val tracks: List<TrackObj>,                    // ByteTrack 결과 (정규화 좌표)
    val violations: List<com.sos.chakhaeng.core.ai.ViolationEvent> // 신호위반(내부 AI 타입)
)