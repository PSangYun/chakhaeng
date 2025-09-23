package com.sos.chakhaeng.domain.usecase.profile

import android.util.Log
import com.sos.chakhaeng.domain.model.profile.UserProfile
import com.sos.chakhaeng.domain.repository.AuthRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<UserProfile> {
        return try {
            val currentUser = authRepository.getCurrentUser()

            if (currentUser != null) {
                val userProfile = UserProfile(
                    id = currentUser.id,
                    name = currentUser.name,
                    title = "교통안전 지킴이",
                    profileImageUrl = currentUser.pictureUrl
                )
                Result.success(userProfile)
            } else {
                val mockUserProfile = UserProfile(
                    id = "user123",
                    name = "김교통",
                    title = "교통안전 지킴이"
                )
                Result.success(mockUserProfile)
            }
        } catch (e: Exception) {
            Log.e("GetUserProfileUseCase", "Error getting user profile", e)
            Result.failure(e)
        }
    }
}