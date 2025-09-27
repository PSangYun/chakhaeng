package com.sos.chakhaeng.domain.model.violation

data class ViolationEvent (
    val type: String,
    val confidence: Float,
    val timeStamp: Long = System.currentTimeMillis(),
    val attrs: Map<String, Any?> = emptyMap(),
    val announceTypes: List<String> = emptyList()
)