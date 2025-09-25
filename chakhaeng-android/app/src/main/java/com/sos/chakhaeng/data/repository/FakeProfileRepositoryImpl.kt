package com.sos.chakhaeng.data.repository

import com.sos.chakhaeng.R
import com.sos.chakhaeng.domain.model.profile.Badge
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

    override suspend fun getUserBadge(): Result<List<Badge>> {
        return try {
            val badgeList = listOf(
                Badge(
                    id = "badge1",
                    name = "교통안전 지킴이",
                    description = "첫 번째 교통위반을 신고 하여 획득",
                    iconRes = R.drawable.badge_safety,
                    isUnlocked = true
                ),
                Badge(
                    id = "badge2",
                    name = "모범 시민",
                    description = "정확도 90% 이상을 달성 하여 획득",
                    iconRes = R.drawable.badge_citizen,
                    isUnlocked = true
                ),
                Badge(
                    id = "badge3",
                    name = "탐지왕",
                    description = "100회 이상 위반 탐지",
                    iconRes = R.drawable.badge_detection,
                    isUnlocked = true
                ),
                Badge(
                    id = "badge4",
                    name = "신고 마스터",
                    description = "50회 이상 정확한 신고",
                    iconRes = R.drawable.badge_report,
                    isUnlocked = true
                )
            )
            Result.success(badgeList)
        }  catch (e: Exception) {
            Result.failure(e)
        }

    }
}