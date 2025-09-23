package com.sos.chakhaeng.domain.model.violation

import com.sos.chakhaeng.domain.model.ViolationType

data class ViolationEvent (
    val id: String,
    val type: ViolationType,
    val detectedAt: Long = System.currentTimeMillis()
)