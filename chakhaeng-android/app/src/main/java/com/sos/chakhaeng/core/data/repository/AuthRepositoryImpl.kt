package com.sos.chakhaeng.core.data.repository

import com.sos.chakhaeng.core.data.datasource.AuthRemoteDataSource
import com.sos.chakhaeng.core.data.mapper.toDomain
import com.sos.chakhaeng.core.datastore.di.GoogleAuthManager
import com.sos.chakhaeng.core.domain.model.User
import com.sos.chakhaeng.core.domain.repository.AuthRepository
import com.sos.chakhaeng.core.session.SessionManager
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val googleAuthManager: GoogleAuthManager,
    private val remote: AuthRemoteDataSource,
    private val session: SessionManager
): AuthRepository {
    override suspend fun signInWithGoogle(idToken: String): Result<User>  = runCatching {
        val res = remote.loginWithGoogle(idToken)
        require(res.success && res.data != null) { res.message ?: "Login failed" }

        session.onLogin(res.data)               // 토큰/유저 저장 + 상태 전파
        res.data.user.toDomain()                // User 반환
    }
}