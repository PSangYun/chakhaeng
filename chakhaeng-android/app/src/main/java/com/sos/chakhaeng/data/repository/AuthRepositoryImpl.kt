package com.sos.chakhaeng.data.repository

import com.google.firebase.messaging.FirebaseMessaging
import com.sos.chakhaeng.data.datasource.remote.AuthRemoteDataSource
import com.sos.chakhaeng.core.session.GoogleAuthManager
import com.sos.chakhaeng.domain.repository.AuthRepository
import com.sos.chakhaeng.core.session.SessionManager
import com.sos.chakhaeng.data.network.api.FcmApi
import com.sos.chakhaeng.data.network.dto.fcm.FcmRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val googleAuthManager: GoogleAuthManager,
    private val remote: AuthRemoteDataSource,
    private val session: SessionManager,
    private val fcmApi: FcmApi
): AuthRepository {
    override suspend fun signInWithGoogle(idToken: String): Result<Boolean>  = runCatching {
        val res = remote.loginWithGoogle(idToken)
        require(res.success && res.data != null) { res.message ?: "Login failed" }

        session.onLogin(res.data)               // 토큰/유저 저장 + 상태 전파
        res.data.firstLogin            // User 반환
    }

    override suspend fun sendFcmToken(newToken: String): Result<Unit> = runCatching {
        val fcmRequest = FcmRequest(newToken,"ANDROID")
        fcmApi.sendToken(request = fcmRequest)
    }
}