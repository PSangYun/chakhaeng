package com.sos.chakhaeng.data.repository

import android.util.Log
import com.sos.chakhaeng.data.datasource.remote.ViolationRemoteDataSource
import com.sos.chakhaeng.data.mapper.ViolationDataMapper.toDomain
import com.sos.chakhaeng.data.mapper.ViolationDataMapper.toRequest
import com.sos.chakhaeng.data.network.dto.request.violation.ViolationRangeRequest
import com.sos.chakhaeng.domain.model.violation.ViolationEntity
import com.sos.chakhaeng.domain.model.violation.ViolationInRangeEntity
import com.sos.chakhaeng.domain.model.violation.ViolationSubmit
import com.sos.chakhaeng.domain.repository.ViolationRepository
import javax.inject.Inject

class ViolationRepositoryImpl @Inject constructor(
    private val remote : ViolationRemoteDataSource,
) : ViolationRepository {

    override suspend fun submitViolation(entity: ViolationEntity): Result<ViolationSubmit> =
        runCatching {
            Log.d("TAG", "submitViolation: 123")
            val req = entity.toRequest()
            val res = remote.submit(req)

            if (res.success && res.data != null) {
                res.data.toDomain()
            } else {
                error(res.message.ifBlank { "신고 전송 실패 (${res.code})" })
            }
        }

    override suspend fun getViolationsInRange(
        from: String,
        to: String
    ): Result<List<ViolationInRangeEntity>> = runCatching {
        val res = remote.getViolationsInRange(
            ViolationRangeRequest(from, to)
        )

        check(res.success) { res.message }
        (res.data ?: emptyList()).map { it.toDomain() }
    }
}