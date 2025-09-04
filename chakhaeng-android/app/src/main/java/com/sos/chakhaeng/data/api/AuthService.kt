package com.sos.chakhaeng.data.api

import com.sos.chakhaeng.data.dto.auth.SignInData
import com.sos.chakhaeng.data.dto.request.GoogleLoginRequest
import com.sos.chakhaeng.data.dto.request.RefreshTokenRequest
import com.sos.chakhaeng.data.remote.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ) : ApiResponse<SignInData>

    @POST("/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ) : ApiResponse<SignInData>

}