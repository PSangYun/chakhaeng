package com.sos.chakhaeng.core.ai

data class LaneDetection (
    val coords: List<List<Pair<Float, Float>>> = emptyList() // LaneDetector 결과
)