package com.sos.chakhaeng.data.datasource.remote

import com.sos.chakhaeng.data.api.AuthService
import com.sos.chakhaeng.data.dto.auth.SignInData
import com.sos.chakhaeng.data.dto.request.GoogleLoginRequest
import com.sos.chakhaeng.data.remote.ApiResponse
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val service: AuthService
) {
    suspend fun loginWithGoogle(idToken: String): ApiResponse<SignInData> {
        val body = GoogleLoginRequest(idToken = idToken)
        return service.loginWithGoogle(body)
    }
}