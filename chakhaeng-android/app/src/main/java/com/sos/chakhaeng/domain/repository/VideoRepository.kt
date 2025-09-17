package com.sos.chakhaeng.domain.repository

import android.net.Uri
import com.sos.chakhaeng.data.network.dto.response.violation.UploadResult
import com.sos.chakhaeng.domain.model.StreamingUrl


interface VideoRepository {
    suspend fun uploadVideo(
        uri: Uri,
        onProgress: (sent: Long, total: Long?) -> Unit = { _, _ -> }
    ): Result<UploadResult>

   suspend fun getStreamingVideoUrl(objectKey: String): StreamingUrl
}