package com.sos.chakhaeng.data.mapper

import com.sos.chakhaeng.R
import com.sos.chakhaeng.data.network.dto.response.profile.BadgeDTO
import com.sos.chakhaeng.data.network.dto.response.profile.UserProfileDTO
import com.sos.chakhaeng.domain.model.profile.Badge
import com.sos.chakhaeng.domain.model.profile.UserProfile

object ProfileDataMapper {
    fun UserProfileDTO.toEntity(): UserProfile = UserProfile(
        name = name,
        title = title,
        profileImageUrl = profileImageUrl
    )

    fun BadgeDTO.toEntity(): Badge = Badge(
        id = id,
        name = name,
        description = description,
        iconRes = name.toDrawableRes(),
        isUnlocked = isUnlocked
    )

    private fun String.toDrawableRes(): Int? = when (this) {
        "교통안전 지킴이" -> R.drawable.badge_safety
        "모범 시민" -> R.drawable.badge_citizen
        "탐지왕" -> R.drawable.badge_detection
        "신고 마스터" -> R.drawable.badge_report
        "연속 출석왕" -> R.drawable.badge_daily_king
        "완벽주의자" -> R.drawable.badge_perfectionist
        "커뮤니티 리더" -> R.drawable.badge_community_leader
        "야간 수호자" -> R.drawable.badge_night_guardian
        "주말 전사" -> R.drawable.badge_weekend_warrior
        "스피드 탐지자" -> R.drawable.badge_speed_detector
        "신호 전문가" -> R.drawable.badge_traffic_expert
        "전설의 신고자" -> R.drawable.badge_legendary_reporter
        "AI 협력자" -> R.drawable.badge_ai_collaborator
        "도시 수호자" -> R.drawable.badge_city_guardian
        "개척자" -> R.drawable.badge_pioneer
        else -> R.drawable.badge_safety // 디폴드 -> "교통안전 지킴이"
    }
}