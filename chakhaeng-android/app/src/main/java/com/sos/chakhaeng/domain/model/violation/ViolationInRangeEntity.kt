package com.sos.chakhaeng.domain.model.violation

data class ViolationInRangeEntity (
    val id: String,
    val videoId: String,
    val violationType: String,
    val plate: String?,
    val locationText: String?,
    val occurredAt: String,
    val createdAt: String
)