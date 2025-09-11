package com.sos.chakhaeng.data.repository

import android.util.Log
import com.sos.chakhaeng.data.mapper.HomeDataMapper.toEntity
import com.sos.chakhaeng.data.network.api.HomeApi
import com.sos.chakhaeng.domain.model.home.RecentViolation
import com.sos.chakhaeng.domain.model.home.TodayStats
import com.sos.chakhaeng.domain.repository.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val homeApi: HomeApi
) : HomeRepository {

    override suspend fun getTodayStats(): Result<TodayStats> {
        return try {
            val response = homeApi.getTodayStats()
            if (response.success) {
                Result.success(response.data?.toEntity() ?: TodayStats(
                    todayDetectionCnt = 0,
                    todayReportCnt = 0
                ))
            } else {
                Result.failure(Exception("Failed to fetch todayStats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentViolation(): Result<List<RecentViolation>> {
        return try {
            val response = homeApi.getRecentViolation(limit = 3)
            if (response.success) {
                val violationList = response.data?.map { it.toEntity() } ?: emptyList()
                Result.success(violationList)
            } else {
                Result.failure(Exception("Failed to fetch recentViolation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}