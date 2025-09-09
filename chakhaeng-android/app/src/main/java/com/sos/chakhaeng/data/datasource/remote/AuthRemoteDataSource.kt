package com.sos.chakhaeng.data.datasource.remote

import com.sos.chakhaeng.data.network.api.AuthApi
import com.sos.chakhaeng.data.network.dto.auth.SignInData
import com.sos.chakhaeng.data.network.dto.request.GoogleLoginRequest
import com.sos.chakhaeng.data.network.dto.ApiResponse
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val service: AuthApi
) {
    suspend fun loginWithGoogle(idToken: String): ApiResponse<SignInData> {
        val body = GoogleLoginRequest(idToken = idToken)
        return service.loginWithGoogle(body)
    }
}