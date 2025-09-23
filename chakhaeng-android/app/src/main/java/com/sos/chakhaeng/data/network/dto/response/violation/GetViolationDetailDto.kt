package com.sos.chakhaeng.data.network.dto.response.violation

data class GetViolationDetailDto (
    val id: String,
    val videoId: String,
    val objectKey: String,
    val type: String,
    val plate: String,
    val locationText: String,
    val occurredAt: String,
    val createdAt: String
)