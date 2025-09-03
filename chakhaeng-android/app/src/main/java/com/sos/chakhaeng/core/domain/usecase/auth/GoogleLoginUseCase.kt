package com.sos.chakhaeng.core.domain.usecase.auth

import com.sos.chakhaeng.core.domain.model.User
import com.sos.chakhaeng.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<Boolean> =
        authRepository.signInWithGoogle(idToken)
}