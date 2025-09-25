package com.sos.chakhaeng.domain.usecase.profile

import com.sos.chakhaeng.domain.model.profile.UserProfile
import com.sos.chakhaeng.domain.repository.ProfileRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): Result<UserProfile> {
        return profileRepository.getUserProfile()
    }
}