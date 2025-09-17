package com.sos.chakhaeng.domain.repository

import com.sos.chakhaeng.domain.model.report.ReportDetailItem
import com.sos.chakhaeng.domain.model.report.ReportItem

interface ReportRepository {
    suspend fun getReportItem(): Result<List<ReportItem>>

    suspend fun deleteReportItem(reportId: String): Result<Unit>

    suspend fun getReportDetailItem(reportId: String) : Result<ReportDetailItem>
}