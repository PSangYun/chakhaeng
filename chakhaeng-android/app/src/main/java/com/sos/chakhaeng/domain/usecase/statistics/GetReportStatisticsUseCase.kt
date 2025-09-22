package com.sos.chakhaeng.domain.usecase.statistics

import com.sos.chakhaeng.domain.model.statistics.ReportStatistics
import com.sos.chakhaeng.domain.repository.StatisticsRepository
import javax.inject.Inject

class GetReportStatisticsUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
){
    suspend operator fun invoke(): Result<ReportStatistics>{
        return statisticsRepository.getReportStatistics()
    }
}