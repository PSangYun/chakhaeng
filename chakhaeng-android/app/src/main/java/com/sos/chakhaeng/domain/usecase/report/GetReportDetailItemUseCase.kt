package com.sos.chakhaeng.domain.usecase.report

import com.sos.chakhaeng.domain.model.report.ReportDetailItem
import com.sos.chakhaeng.domain.repository.ReportRepository
import javax.inject.Inject

class GetReportDetailItemUseCase @Inject constructor(
    private val reportRepository: ReportRepository
){
    suspend operator fun invoke(reportId: String): Result<ReportDetailItem> {
        return reportRepository.getReportDetailItem(reportId)
    }
}