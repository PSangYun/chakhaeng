package com.sos.chakhaeng.domain.usecase.violation

import com.sos.chakhaeng.domain.repository.ViolationRepository
import javax.inject.Inject

class GetViolationsInRangeUseCase @Inject constructor(
    private val repository: ViolationRepository
){
    suspend operator fun invoke(from: String, to: String) = repository.getViolationsInRange(from, to)
}