package com.sos.chakhaeng.domain.usecase.video

import android.net.Uri
import com.sos.chakhaeng.data.network.dto.response.violation.UploadResult
import com.sos.chakhaeng.domain.repository.VideoRepository
import javax.inject.Inject

class UploadVideoUseCase @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(
        uri: Uri,
        onProgress: (sent: Long, total: Long?) -> Unit = { _, _ -> }
    ): Result<UploadResult> = repository.uploadVideo(uri, onProgress)
}