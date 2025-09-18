package com.sos.chakhaeng.domain.model.violation

data class ViolationEvent (
    val type: String,
    val confidence: Float,
    val timeStamp: Long = System.currentTimeMillis()
)