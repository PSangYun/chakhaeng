package com.sos.chakhaeng.data.network.dto.response.violation

data class UploadResult (
    val uploadUrl: UploadUrl,
    val complete: CompleteUploadResponse
)