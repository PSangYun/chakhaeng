package com.sos.chakhaeng.core.domain.repository

import com.sos.chakhaeng.core.data.model.auth.SignInData
import com.sos.chakhaeng.core.data.remote.ApiResponse
import com.sos.chakhaeng.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<Boolean>
}