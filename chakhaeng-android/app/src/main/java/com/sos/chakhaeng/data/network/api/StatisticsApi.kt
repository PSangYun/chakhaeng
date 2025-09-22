package com.sos.chakhaeng.data.network.api

import com.sos.chakhaeng.data.network.dto.ApiResponse
import com.sos.chakhaeng.data.network.dto.response.statistics.ReportStatisticsDTO
import com.sos.chakhaeng.data.network.dto.response.statistics.ViolationStatisticsDTO
import retrofit2.http.GET

interface StatisticsApi {
    @GET("statistics/violation")
    suspend fun getViolationStatistics(): ApiResponse<ViolationStatisticsDTO>

    @GET("statistics/report")
    suspend fun getReportStatistics(): ApiResponse<ReportStatisticsDTO>
}