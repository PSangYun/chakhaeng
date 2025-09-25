package com.sos.chakhaeng.data.repository

import com.sos.chakhaeng.data.mapper.ProfileDataMapper.toEntity
import com.sos.chakhaeng.data.network.api.ProfileApi
import com.sos.chakhaeng.domain.model.profile.UserProfile
import com.sos.chakhaeng.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi
) : ProfileRepository {
    override suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val response = profileApi.getUserProfile()
            if (response.success) {
                val userProfile = response.data?.toEntity() ?: UserProfile(
                    name ="",
                    title = "",
                    profileImageUrl = ""
                )
                Result.success(userProfile)
            } else {
                Result.failure(RuntimeException("사용자 프로필 정보 가져오기 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}