package com.sos.chakhaeng.data.mapper

import com.sos.chakhaeng.data.network.dto.request.violation.ViolationRequest
import com.sos.chakhaeng.data.network.dto.response.violation.ViolationSubmitResponse
import com.sos.chakhaeng.domain.model.violation.ViolationEntity
import com.sos.chakhaeng.domain.model.violation.ViolationSubmit

object ViolationDataMapper {

    fun ViolationEntity.toRequest() = ViolationRequest(
        violationType = violationType,
        location      = location,
        title         = title,
        description   = description,
        plateNumber   = plateNumber,
        date          = date,
        time          = time,
        videoId       = videoUrl
    )

    fun ViolationSubmitResponse.toDomain() = ViolationSubmit(
        id = id,
        status = status
    )
}