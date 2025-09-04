package com.sos.chakhaeng.domain.usecase.auth

import com.sos.chakhaeng.domain.repository.AuthRepository
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<Boolean> =
        authRepository.signInWithGoogle(idToken)
}