package com.sos.chakhaeng.domain.usecase.report

import com.sos.chakhaeng.domain.repository.ReportRepository
import javax.inject.Inject

class DeleteReportItemUseCase @Inject constructor(
    private val reportRepository: ReportRepository
){
    suspend operator fun invoke(reportId: String): Result<Unit> {
        return reportRepository.deleteReportItem(reportId)
    }
}