package com.sos.chakhaeng.data.repository

import com.sos.chakhaeng.data.mapper.ReportDataMapper.toEntity
import com.sos.chakhaeng.data.network.api.ReportApi
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.report.ReportItem
import com.sos.chakhaeng.domain.model.report.ReportStatus
import com.sos.chakhaeng.domain.repository.ReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportApi: ReportApi
) : ReportRepository {

    override suspend fun getReportItem(): Result<List<ReportItem>> {
        return try {
            val response = reportApi.getReportItem()
            if (response.success) {
                val reportItems = response.data?.map { it.toEntity() } ?: emptyList()
                Result.success(reportItems)
            } else {
                Result.failure(RuntimeException("신고 아이템 불러오기 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReportItem(reportId: String): Result<Unit> {
        return try {
            val response = reportApi.deleteReportItem(reportId)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(RuntimeException("신고 아이템 삭제 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        return Result.success(Unit)
    }
}