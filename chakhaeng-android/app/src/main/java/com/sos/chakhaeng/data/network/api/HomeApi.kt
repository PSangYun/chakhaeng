package com.sos.chakhaeng.data.network.api

import com.sos.chakhaeng.data.network.dto.ApiResponse
import com.sos.chakhaeng.data.network.dto.response.home.RecentViolationDTO
import com.sos.chakhaeng.data.network.dto.response.home.TodayStatsDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApi {
    @GET("home/today")
    suspend fun getTodayStats(): ApiResponse<TodayStatsDTO>

    @GET("home/recent")
    suspend fun getRecentViolation(
        @Query("limit") limit: Int = 3
    ): ApiResponse<List<RecentViolationDTO>>
}