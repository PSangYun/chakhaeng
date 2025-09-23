package com.sos.chakhaeng.domain.repository

import com.sos.chakhaeng.domain.model.statistics.ReportStatistics
import com.sos.chakhaeng.domain.model.statistics.ViolationStatistics

interface StatisticsRepository {
    suspend fun getViolationStatistics(): Result<ViolationStatistics>

    suspend fun getReportStatistics(): Result<ReportStatistics>
}