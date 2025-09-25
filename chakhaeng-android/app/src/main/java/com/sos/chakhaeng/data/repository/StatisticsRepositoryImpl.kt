package com.sos.chakhaeng.data.repository

import android.util.Log
import com.sos.chakhaeng.data.mapper.StatisticsDataMapper.toEntity
import com.sos.chakhaeng.data.network.api.StatisticsApi
import com.sos.chakhaeng.domain.model.statistics.ReportStatistics
import com.sos.chakhaeng.domain.model.statistics.ViolationStatistics
import com.sos.chakhaeng.domain.repository.StatisticsRepository
import javax.inject.Inject

class StatisticsRepositoryImpl @Inject constructor(
    private val statisticsApi: StatisticsApi
) : StatisticsRepository{
    override suspend fun getViolationStatistics(): Result<ViolationStatistics> {
        return try {
            val response = statisticsApi.getViolationStatistics()
            if(response.success) {
                Result.success(response.data?.toEntity() ?: throw Exception("ViolationStatistics 데이터가 없습니다."))
            } else {
                Log.d("test221",response.success.toString())
                Result.failure(Exception("ViolationStatistics을 불러오는데 실패하였습니다."))
            }
        } catch (e: Exception) {
            Log.d("test221",e.message,e)
            Result.failure(e)
        }
    }

    override suspend fun getReportStatistics(): Result<ReportStatistics> {
        return try {
            val response = statisticsApi.getReportStatistics()
            if (response.success) {
                Result.success(response.data?.toEntity() ?: throw Exception("ReportStatistics 데이터가 없습니다."))
            } else {
                Result.failure(Exception("ReportStatistics을 불러오는데 실패하였습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}