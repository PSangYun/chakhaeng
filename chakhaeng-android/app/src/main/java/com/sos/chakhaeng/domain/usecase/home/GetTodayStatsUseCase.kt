package com.sos.chakhaeng.domain.usecase.home

import com.sos.chakhaeng.domain.model.home.TodayStats
import com.sos.chakhaeng.domain.repository.HomeRepository
import javax.inject.Inject

class GetTodayStatsUseCase @Inject constructor(
    private val homeRepository: HomeRepository
){
    suspend operator fun invoke(): Result<TodayStats> {
        return homeRepository.getTodayStats()
    }
}