package com.sos.chakhaeng.data.network.api

import com.sos.chakhaeng.data.network.dto.fcm.FcmRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {
    @POST("fcm/register")
    suspend fun sendToken(@Body request: FcmRequest)
}