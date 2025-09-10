package com.sos.chakhaeng.domain.usecase.violation

import com.sos.chakhaeng.domain.model.violation.ViolationEntity
import com.sos.chakhaeng.domain.model.violation.ViolationSubmit
import com.sos.chakhaeng.domain.repository.ViolationRepository
import javax.inject.Inject

class SubmitViolationUseCase @Inject constructor(
    private val repository: ViolationRepository
) {
    suspend operator fun invoke(entity: ViolationEntity): Result<ViolationSubmit> =
        repository.submitViolation(entity)
}