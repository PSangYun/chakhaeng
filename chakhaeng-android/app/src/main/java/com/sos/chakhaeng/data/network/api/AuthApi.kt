package com.sos.chakhaeng.data.network.api

import com.sos.chakhaeng.data.network.dto.auth.SignInData
import com.sos.chakhaeng.data.network.dto.request.GoogleLoginRequest
import com.sos.chakhaeng.data.network.dto.request.RefreshTokenRequest
import com.sos.chakhaeng.data.network.dto.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ) : ApiResponse<SignInData>

    @POST("/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ) : ApiResponse<SignInData>

}