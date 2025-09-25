package com.sos.chakhaeng.data.repository

import com.sos.chakhaeng.domain.model.profile.UserProfile
import com.sos.chakhaeng.domain.repository.ProfileRepository
import javax.inject.Inject

class FakeProfileRepositoryImpl @Inject constructor(
) : ProfileRepository {

    override suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val userProfile = UserProfile(
                name = "유저네임",
                title = "교통안전 지킴이",
                profileImageUrl = "https://lh3.googleusercontent.com/a/ACg8ocLQZ9OYppMbSYMcSQkSqBW2Auk6FN9Hro1jI6kwrwhcDj8VyA=s96-c"
            )
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}