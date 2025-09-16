package com.sos.chakhaeng.data.network.api

import com.sos.chakhaeng.data.network.dto.ApiResponse
import com.sos.chakhaeng.data.network.dto.response.reoprt.ReportDetailItemDTO
import com.sos.chakhaeng.data.network.dto.response.reoprt.ReportItemDTO
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface ReportApi {
    @GET("report")
    suspend fun getReportItem(): ApiResponse<List<ReportItemDTO>>

    @DELETE("report/{reportId}/delete")
    suspend fun deleteReportItem(
        @Path("reportId") reportId: String
    ) : ApiResponse<Unit>

    @GET("report/{reportId}")
    suspend fun getReportDetailItem(
        @Path("reportId") reportId: String
    ): ApiResponse<ReportDetailItemDTO>
}