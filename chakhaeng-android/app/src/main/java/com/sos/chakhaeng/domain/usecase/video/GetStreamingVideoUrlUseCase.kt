package com.sos.chakhaeng.domain.usecase.video

import com.sos.chakhaeng.domain.model.StreamingUrl
import com.sos.chakhaeng.domain.repository.VideoRepository
import javax.inject.Inject

class GetStreamingVideoUrlUseCase @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(objectKey: String): StreamingUrl =
        repository.getStreamingVideoUrl(objectKey)
}