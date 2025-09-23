package com.sos.chakhaeng.core.ai

/**
 * Lane Detection 전용 모델 메타데이터
 */
data class LaneModelSpec(
    val key: String = "lane",
    val assetPath: String = "models/culane_res18_dynamic.tflite",

    // 입력 크기 (culane_res18 기준)
    val preferInputW: Int = 1600,
    val preferInputH: Int = 320,

    // 모델 출력 관련 shape
    val numRow: Int = 72,         // exist_row
    val numCol: Int = 81,         // exist_col
    val numCellRow: Int = 200,    // loc_row
    val numCellCol: Int = 100,    // loc_col
    val numLanes: Int = 4,

    // crop 비율
    val cropRatio: Float = 0.6f
)
