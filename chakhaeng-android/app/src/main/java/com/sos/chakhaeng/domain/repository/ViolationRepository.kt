package com.sos.chakhaeng.domain.repository

import com.sos.chakhaeng.domain.model.violation.GetViolationDetail
import com.sos.chakhaeng.domain.model.violation.ViolationEntity
import com.sos.chakhaeng.domain.model.violation.ViolationInRangeEntity
import com.sos.chakhaeng.domain.model.violation.ViolationSubmit

interface ViolationRepository {
    suspend fun submitViolation(entity: ViolationEntity): Result<ViolationSubmit>

    suspend fun getViolationsInRange(from: String, to: String): Result<List<ViolationInRangeEntity>>

    suspend fun getViolationDetail(violationId: String): Result<GetViolationDetail>

    suspend fun detectViolation(
        videoId: String,
        type: String,
        plate: String,
        locationText: String,
        occurredAt: String
    ) : Result<Unit>
}