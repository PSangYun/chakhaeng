package com.sos.chakhaeng.data.network.dto.response.violation

data class CompleteUploadResponse (
    val id: String,
    val objectKey: String,
    val originalName: String,
    val status: String,
    val createdAt: String
)