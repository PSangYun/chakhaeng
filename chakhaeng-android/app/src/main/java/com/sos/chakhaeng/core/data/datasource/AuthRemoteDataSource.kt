// core/data/datasource/AuthRemoteDataSource.kt
package com.sos.chakhaeng.core.data.datasource

import com.sos.chakhaeng.core.data.model.auth.SignInData
import com.sos.chakhaeng.core.data.model.request.GoogleLoginRequest
import com.sos.chakhaeng.core.data.service.AuthService
import com.sos.chakhaeng.core.data.remote.ApiResponse
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val service: AuthService
) {
    suspend fun loginWithGoogle(idToken: String): ApiResponse<SignInData> {
        val body = GoogleLoginRequest(idToken = idToken)
        return service.loginWithGoogle(body)
    }
}
