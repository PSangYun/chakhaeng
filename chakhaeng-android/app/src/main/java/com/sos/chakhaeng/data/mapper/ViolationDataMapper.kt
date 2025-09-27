package com.sos.chakhaeng.data.mapper

import com.sos.chakhaeng.data.network.dto.request.violation.ViolationRequest
import com.sos.chakhaeng.data.network.dto.response.violation.DetectViolationResponse
import com.sos.chakhaeng.data.network.dto.response.violation.GetViolationDetailDto
import com.sos.chakhaeng.data.network.dto.response.violation.ViolationDto
import com.sos.chakhaeng.data.network.dto.response.violation.ViolationSubmitResponse
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.violation.GetViolationDetail
import com.sos.chakhaeng.domain.model.violation.ViolationEntity
import com.sos.chakhaeng.domain.model.violation.ViolationInRangeEntity
import com.sos.chakhaeng.domain.model.violation.ViolationSubmit
import kotlin.random.Random

object ViolationDataMapper {

    fun ViolationEntity.toRequest() = ViolationRequest(
        violationType = violationType,
        location = location,
        title = title,
        description = description,
        plateNumber = plateNumber,
        date = date,
        time = time,
        videoId = videoUrl
    )

    fun ViolationSubmitResponse.toDomain() = ViolationSubmit(
        id = id,
        status = status
    )

    fun ViolationDto.toDomain() = ViolationInRangeEntity(
        id = id,
        videoId = videoId,
        violationType = type.toViolationType(),
        plate = plate,
        locationText = locationText,
        occurredAt = occurredAt,
        createdAt = createdAt
    )

    fun GetViolationDetailDto.toDomain() = GetViolationDetail(
        id = id,
        videoId = videoId,
        objectKey = objectKey,
        type = type,
        plate = plate,
        locationText = locationText,
        occurredAt = occurredAt,
        createdAt = createdAt
    )
    private fun String.toViolationType(): ViolationType = when (this.uppercase()) {
        "역주행" -> ViolationType.WRONG_WAY
        "킥보드 2인이상" -> ViolationType.LOVE_BUG
        "신호위반" -> ViolationType.SIGNAL
        "차선침범" -> ViolationType.LANE
        "무번호판" -> ViolationType.NO_PLATE
        "헬멧 미착용" -> ViolationType.NO_HELMET
        "헬멧 미착용·중앙선 침범" -> ViolationType.NO_HELMET_AND_LANE
        "킥보드 2인이상·헬멧 미착용" -> ViolationType.NO_HELMET_AND_LOVE_BUG
        else -> ViolationType.OTHERS
    }
}