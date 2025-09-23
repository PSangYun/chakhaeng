package com.sos.chakhaeng.data.datasource.remote

import android.util.Log
import com.sos.chakhaeng.data.mapper.ViolationDataMapper.toRequest
import com.sos.chakhaeng.data.network.api.ViolationApi
import com.sos.chakhaeng.data.network.dto.ApiResponse
import com.sos.chakhaeng.data.network.dto.request.violation.ViolationRangeRequest
import com.sos.chakhaeng.data.network.dto.request.violation.ViolationRequest
import com.sos.chakhaeng.data.network.dto.response.violation.GetViolationDetailDto
import com.sos.chakhaeng.data.network.dto.response.violation.ViolationDto
import com.sos.chakhaeng.data.network.dto.response.violation.ViolationSubmitResponse
import com.sos.chakhaeng.domain.model.violation.ViolationEntity
import javax.inject.Inject

class ViolationRemoteDataSource @Inject constructor(
    private val service: ViolationApi
) {
    suspend fun submit(request: ViolationRequest): ApiResponse<ViolationSubmitResponse> {
        Log.d("TAG", "submit: 1234")
        return service.submitViolation(request)
    }

    suspend fun getViolationsInRange(request: ViolationRangeRequest): ApiResponse<List<ViolationDto>> {
        return service.getViolationsInRange(request)
    }

    suspend fun getViolationDetail(violationId: String): ApiResponse<GetViolationDetailDto> {
        return service.getViolationDetail(violationId)
    }
}
