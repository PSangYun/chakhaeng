package com.sos.chakhaeng.domain.repository

import com.sos.chakhaeng.core.session.AuthState
import com.sos.chakhaeng.domain.model.User

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<Boolean>
    suspend fun setGoogleUser(user: User?)
    suspend fun getCurrentUser(): User?
    suspend fun getCurrentAuthState(): AuthState

    suspend fun sendFcmToken(newToken: String): Result<Unit>
}