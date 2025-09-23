package com.sos.chakhaeng.data.repository

import com.sos.chakhaeng.core.session.AuthState
import com.sos.chakhaeng.data.datasource.remote.AuthRemoteDataSource
import com.sos.chakhaeng.domain.repository.AuthRepository
import com.sos.chakhaeng.core.session.SessionManager
import com.sos.chakhaeng.domain.model.User
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remote: AuthRemoteDataSource,
    private val session: SessionManager
): AuthRepository {

    // Google 사용자 정보를 저장하기 위한 임시 변수
    private var currentGoogleUser: User? = null

    override suspend fun signInWithGoogle(idToken: String): Result<Boolean>  = runCatching {
        val res = remote.loginWithGoogle(idToken)
        require(res.success && res.data != null) { res.message }

        // Google 사용자 정보와 함께 세션 저장
        session.onLogin(res.data, currentGoogleUser)
        res.data.firstLogin
    }

    // Google 사용자 정보를 설정하는 메서드 추가
    override suspend fun setGoogleUser(user: User?) {
        currentGoogleUser = user
    }

    override suspend fun getCurrentUser(): User? {
        return when (val authState = session.authState.value) {
            is AuthState.Authenticated -> authState.user
            else -> null
        }
    }

    override suspend fun getCurrentAuthState(): AuthState {
        return session.authState.value
    }
}