package com.sos.chakhaeng.domain.usecase.home

import com.sos.chakhaeng.domain.model.home.RecentViolation
import com.sos.chakhaeng.domain.repository.HomeRepository
import javax.inject.Inject

class GetRecentViolationUseCase @Inject constructor(
    private val homeRepository: HomeRepository
){
    suspend operator fun invoke(): Result<List<RecentViolation>> {
        return homeRepository.getRecentViolation()
    }
}