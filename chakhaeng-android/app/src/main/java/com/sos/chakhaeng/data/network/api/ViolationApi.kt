package com.sos.chakhaeng.data.network.api

import com.sos.chakhaeng.data.network.dto.ApiResponse
import com.sos.chakhaeng.data.network.dto.request.violation.ViolationRangeRequest
import com.sos.chakhaeng.data.network.dto.request.violation.ViolationRequest
import com.sos.chakhaeng.data.network.dto.response.violation.ViolationDto
import com.sos.chakhaeng.data.network.dto.response.violation.ViolationSubmitResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ViolationApi {

    @POST("report/create-report")
    suspend fun submitViolation(
        @Body request: ViolationRequest
    ): ApiResponse<ViolationSubmitResponse>

    @POST("violation/range")
    suspend fun getViolationsInRange(
        @Body request: ViolationRangeRequest
    ): ApiResponse<List<ViolationDto>>
}