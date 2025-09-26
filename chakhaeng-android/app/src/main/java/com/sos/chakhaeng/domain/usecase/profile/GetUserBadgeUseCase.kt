package com.sos.chakhaeng.domain.usecase.profile

import com.sos.chakhaeng.domain.model.profile.Badge
import com.sos.chakhaeng.domain.repository.ProfileRepository
import javax.inject.Inject

class GetUserBadgeUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
){
    suspend operator fun invoke(): Result<List<Badge>> {
        return profileRepository.getUserBadge()
    }
}