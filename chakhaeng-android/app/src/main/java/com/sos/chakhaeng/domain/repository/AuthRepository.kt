package com.sos.chakhaeng.domain.repository

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<Boolean>

    suspend fun sendFcmToken(newToken: String): Result<Unit>
}