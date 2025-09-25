package com.sos.chakhaeng.data.network.dto.response.violation

data class DetectViolationResponse (
    val id: String,
    val videoId: String,
    val type: String,
    val plate: String,
    val locationText: String,
    val occurredAt: String,
    val createdAt: String
)