package com.sos.chakhaeng.data.network.dto.request.violation

data class CompleteUploadRequest (
    val objectKey: String,
    val originalName: String,
    val contentType: String,
    val sizeBytes: Long,
    val durationSec: Long
)