package com.sos.chakhaeng.domain.usecase.profile

import com.sos.chakhaeng.domain.model.profile.Mission
import com.sos.chakhaeng.domain.repository.ProfileRepository
import javax.inject.Inject

class GetAllMissionUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
){
    suspend operator fun invoke(): Result<List<Mission>> {
        return profileRepository.getAllMissions()
    }
}