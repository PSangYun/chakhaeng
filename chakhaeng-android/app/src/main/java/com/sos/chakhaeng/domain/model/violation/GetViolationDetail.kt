package com.sos.chakhaeng.domain.model.violation


data class GetViolationDetail (
    val id: String,
    val videoId: String,
    val objectKey: String,
    val type: String,
    val plate: String,
    val locationText: String,
    val occurredAt: String,
    val createdAt: String
)