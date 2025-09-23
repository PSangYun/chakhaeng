package com.sos.chakhaeng.domain.usecase.statistics

import com.sos.chakhaeng.domain.model.statistics.ViolationStatistics
import com.sos.chakhaeng.domain.repository.StatisticsRepository
import javax.inject.Inject

class GetViolationStatisticsUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
){
    suspend operator fun invoke(): Result<ViolationStatistics> {
        return statisticsRepository.getViolationStatistics()
    }
}