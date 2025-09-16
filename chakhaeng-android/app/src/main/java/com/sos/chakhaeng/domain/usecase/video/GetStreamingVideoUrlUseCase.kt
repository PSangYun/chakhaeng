package com.sos.chakhaeng.domain.usecase.video

import com.sos.chakhaeng.data.network.dto.response.violation.UploadUrl
import com.sos.chakhaeng.domain.repository.VideoRepository
import javax.inject.Inject

class GetStreamingVideoUrlUseCase @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(objectKey: String): UploadUrl =
        repository.getStreamingVideoUrl(objectKey)
}