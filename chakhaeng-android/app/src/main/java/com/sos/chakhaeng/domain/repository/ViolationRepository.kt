package com.sos.chakhaeng.domain.repository

import com.sos.chakhaeng.domain.model.violation.ViolationEntity
import com.sos.chakhaeng.domain.model.violation.ViolationSubmit

interface ViolationRepository {
    suspend fun submitViolation(entity: ViolationEntity): Result<ViolationSubmit>
}