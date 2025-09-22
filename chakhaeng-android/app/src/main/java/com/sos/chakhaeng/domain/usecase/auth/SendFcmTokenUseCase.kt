package com.sos.chakhaeng.domain.usecase.auth

import com.sos.chakhaeng.domain.repository.AuthRepository
import javax.inject.Inject

class SendFcmTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(fcmToken: String): Result<Unit> =
        authRepository.sendFcmToken(fcmToken)
}