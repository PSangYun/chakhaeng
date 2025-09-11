package com.sos.chakhaeng.domain.usecase.report

import com.sos.chakhaeng.domain.model.report.ReportItem
import com.sos.chakhaeng.domain.repository.ReportRepository
import javax.inject.Inject

class GetReportItemUseCase @Inject constructor(
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(): Result<List<ReportItem>> {
        return reportRepository.getReportItem()
    }
}