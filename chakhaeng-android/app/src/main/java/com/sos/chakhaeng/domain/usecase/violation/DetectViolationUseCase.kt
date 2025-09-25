package com.sos.chakhaeng.domain.usecase.violation

import android.net.Uri
import com.sos.chakhaeng.domain.model.location.Location
import com.sos.chakhaeng.domain.repository.LocationRepository
import com.sos.chakhaeng.domain.repository.VideoRepository
import com.sos.chakhaeng.domain.repository.ViolationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DetectViolationUseCase @Inject constructor(
    private val violationRepository: ViolationRepository,
    private val videoRepository: VideoRepository,
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(
        uri: Uri,
        onProgress: (sent: Long, total: Long?) -> Unit = { _, _ -> },
        lat: Double,
        lng: Double,
        type: String,
        plate: String,
        occurredAt: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        videoRepository.uploadVideo(uri, onProgress)
            .flatMap { uploadResult ->
                val locationText = locationRepository.getAddressFromLocation(Location(lat, lng))
                    ?.fullAddress
                    ?: "구미시 진평동 543-2"

                Result.success(Pair(uploadResult.complete.id, locationText))
            }
            .flatMap { (videoId, locationText) ->
                violationRepository.detectViolation(
                    videoId = videoId,
                    type = type,
                    plate = plate,
                    locationText = locationText,
                    occurredAt = occurredAt
                )
            }
            .map { Unit }
    }
}

inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    fold(onSuccess = { transform(it) }, onFailure = { Result.failure(it) })
