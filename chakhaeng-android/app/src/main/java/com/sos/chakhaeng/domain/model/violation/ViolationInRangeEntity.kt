package com.sos.chakhaeng.domain.model.violation

import com.sos.chakhaeng.domain.model.ViolationType

data class ViolationInRangeEntity (
    val id: String,
    val videoId: String,
    val violationType: ViolationType,
    val plate: String?,
    val locationText: String?,
    val occurredAt: String,
    val createdAt: String
)