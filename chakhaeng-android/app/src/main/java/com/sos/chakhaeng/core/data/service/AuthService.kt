package com.sos.chakhaeng.core.data.service

import com.sos.chakhaeng.core.data.model.auth.SignInData
import com.sos.chakhaeng.core.data.model.request.GoogleLoginRequest
import com.sos.chakhaeng.core.data.remote.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ) : ApiResponse<SignInData>
}