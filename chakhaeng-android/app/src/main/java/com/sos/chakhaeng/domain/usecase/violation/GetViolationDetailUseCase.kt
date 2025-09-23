package com.sos.chakhaeng.domain.usecase.violation

import com.sos.chakhaeng.domain.model.violation.GetViolationDetail
import com.sos.chakhaeng.domain.repository.ViolationRepository
import javax.inject.Inject

class GetViolationDetailUseCase @Inject constructor(
    private val repo: ViolationRepository
) {
    suspend operator fun invoke(violationId: String): Result<GetViolationDetail> =
        repo.getViolationDetail(violationId)
}