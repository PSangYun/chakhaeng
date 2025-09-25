package com.sos.chakhaeng.data.repository

import com.sos.chakhaeng.R
import com.sos.chakhaeng.domain.model.profile.Badge
import com.sos.chakhaeng.domain.model.profile.Mission
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
                    id = "badge_safety_keeper",
                    name = "교통안전 지킴이",
                    description = "첫 번째 교통위반을 신고하여 획득",
                    iconRes = R.drawable.badge_safety,
                    isUnlocked = true
                ),
                Badge(
                    id = "badge_model_citizen",
                    name = "모범 시민",
                    description = "정확도 90% 이상을 달성하여 획득",
                    iconRes = R.drawable.badge_citizen,
                    isUnlocked = true
                ),
                Badge(
                    id = "badge_detection_master",
                    name = "탐지왕",
                    description = "100회 이상 위반 탐지하여 획득",
                    iconRes = R.drawable.badge_detection,
                    isUnlocked = true
                ),
                Badge(
                    id = "badge_report_master",
                    name = "신고 마스터",
                    description = "50회 이상 정확한 신고하여 획득",
                    iconRes = R.drawable.badge_report,
                    isUnlocked = true
                ),

                // 아직 획득하지 못한 배지들 (활동 관련)
                Badge(
                    id = "badge_attendance_king",
                    name = "연속 출석왕",
                    description = "30일 연속 앱 사용하여 획득",
                    iconRes = R.drawable.badge_daily_king,
                    isUnlocked = false
                ),
                Badge(
                    id = "badge_perfectionist",
                    name = "완벽주의자",
                    description = "정확도 95% 이상 달성하여 획득",
                    iconRes = R.drawable.badge_perfectionist,
                    isUnlocked = false
                ),
                Badge(
                    id = "badge_community_leader",
                    name = "커뮤니티 리더",
                    description = "다른 사용자에게 10회 이상 도움을 주어 획득",
                    iconRes = R.drawable.badge_community_leader,
                    isUnlocked = false
                ),

                // 고급 배지들 (특별 활동)
                Badge(
                    id = "badge_night_guardian",
                    name = "야간 수호자",
                    description = "밤 시간대(22:00-06:00) 100회 탐지하여 획득",
                    iconRes = R.drawable.badge_night_guardian,
                    isUnlocked = false
                ),
                Badge(
                    id = "badge_weekend_warrior",
                    name = "주말 전사",
                    description = "주말에만 200회 신고하여 획득",
                    iconRes = R.drawable.badge_weekend_warrior,
                    isUnlocked = false
                ),
                Badge(
                    id = "badge_speed_detector",
                    name = "스피드 탐지자",
                    description = "속도위반 전문 탐지 50회하여 획득",
                    iconRes = R.drawable.badge_speed_detector,
                    isUnlocked = false
                ),
                Badge(
                    id = "badge_signal_expert",
                    name = "신호 전문가",
                    description = "신호위반 전문 신고 100회하여 획득",
                    iconRes = R.drawable.badge_traffic_expert,
                    isUnlocked = false
                ),

                // 희귀 배지들 (특별 업적)
                Badge(
                    id = "badge_legendary_reporter",
                    name = "전설의 신고자",
                    description = "1000회 신고 달성하여 획득하는 레전드 배지",
                    iconRes = R.drawable.badge_legendary_reporter,
                    isUnlocked = false
                ),
                Badge(
                    id = "badge_ai_collaborator",
                    name = "AI 협력자",
                    description = "AI 탐지 정확도 향상에 기여하여 획득",
                    iconRes = R.drawable.badge_ai_collaborator,
                    isUnlocked = false
                ),
                Badge(
                    id = "badge_city_protector",
                    name = "도시 수호자",
                    description = "우리 동네 교통안전을 크게 개선하여 획득",
                    iconRes = R.drawable.badge_city_guardian,
                    isUnlocked = false
                ),
                Badge(
                    id = "badge_pioneer",
                    name = "개척자",
                    description = "새로운 기능을 가장 먼저 사용하여 획득",
                    iconRes = R.drawable.badge_pioneer,
                    isUnlocked = false
                )
            )
            Result.success(badgeList)
        }  catch (e: Exception) {
            Result.failure(e)
        }

    }

    override suspend fun getRecentCompletedMissions(): Result<List<Mission>> {
        return try {
            val missionList = listOf(
                Mission(
                    id = "mission1",
                    title = "첫 번째 위반 탐지",
                    description = "교통위반을 1회 탐지하세요",
                    iconRes = R.drawable.ic_target_gd,
                    isCompleted = true
                ),
                Mission(
                    id = "mission2",
                    title = "정확한 신고",
                    description = "정확한 신고를 5회 완료하세요",
                    iconRes = R.drawable.ic_report_gd,
                    isCompleted = true
                ),
                Mission(
                    id = "mission3",
                    title = "연속 사용",
                    description = "7일 연속으로 앱을 사용하세요",
                    iconRes = R.drawable.ic_calendar,
                    isCompleted = false
                ),
            )
            Result.success(missionList)
        } catch(e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllMissions(): Result<List<Mission>> {
        return try {
            val missionList = listOf(
                Mission(
                    id = "mission_first_detection",
                    title = "첫 번째 위반 탐지",
                    description = "교통위반을 1회 탐지",
                    iconRes = R.drawable.badge_safety,
                    isCompleted = true,
                    currentProgress = 1,
                    targetProgress = 1,
                    rewardName = "교통안전 지킴이 배지"
                ),
                Mission(
                    id = "mission_accurate_reports",
                    title = "정확한 신고",
                    description = "정확한 신고를 5회 완료",
                    iconRes = R.drawable.badge_citizen,
                    isCompleted = true,
                    currentProgress = 5,
                    targetProgress = 5,
                    rewardName = "모범 시민 배지"
                ),
                Mission(
                    id = "mission_detection_master_100",
                    title = "탐지 마스터",
                    description = "위반을 100회 탐지",
                    iconRes = R.drawable.badge_detection,
                    isCompleted = true,
                    currentProgress = 100,
                    targetProgress = 100,
                    rewardName = "탐지왕 배지"
                ),
                Mission(
                    id = "mission_report_master_50",
                    title = "신고 마스터",
                    description = "정확한 신고를 50회 완료",
                    iconRes = R.drawable.badge_report,
                    isCompleted = true,
                    currentProgress = 50,
                    targetProgress = 50,
                    rewardName = "신고 마스터 배지"
                ),

                Mission(
                    id = "mission_consecutive_usage",
                    title = "연속 사용",
                    description = "30일 연속으로 앱을 사용",
                    iconRes = R.drawable.badge_daily_king,
                    isCompleted = false,
                    currentProgress = 18,
                    targetProgress = 30,
                    rewardName = "연속 출석왕 배지"
                ),
                Mission(
                    id = "mission_perfect_accuracy",
                    title = "완벽주의자",
                    description = "신고 정확도 95% 이상 달성",
                    iconRes = R.drawable.badge_perfectionist,
                    isCompleted = false,
                    currentProgress = 87,
                    targetProgress = 95,
                    rewardName = "완벽주의자 배지"
                ),
                Mission(
                    id = "mission_community_helper",
                    title = "커뮤니티 도우미",
                    description = "다른 사용자에게 10회 이상 도움",
                    iconRes = R.drawable.badge_community_leader,
                    isCompleted = false,
                    currentProgress = 6,
                    targetProgress = 10,
                    rewardName = "커뮤니티 리더 배지"
                ),
                Mission(
                    id = "mission_night_patrol",
                    title = "야간 순찰",
                    description = "밤 시간대(22:00-06:00)에 100회 탐지",
                    iconRes = R.drawable.badge_night_guardian,
                    isCompleted = false,
                    currentProgress = 23,
                    targetProgress = 100,
                    rewardName = "야간 수호자 배지"
                ),
                Mission(
                    id = "mission_weekend_warrior",
                    title = "주말 활동가",
                    description = "주말에 200회 신고",
                    iconRes = R.drawable.badge_weekend_warrior,
                    isCompleted = false,
                    currentProgress = 85,
                    targetProgress = 200,
                    rewardName = "주말 전사 배지"
                ),
                Mission(
                    id = "mission_speed_specialist",
                    title = "속도위반 전문가",
                    description = "속도위반을 50회 탐지",
                    iconRes = R.drawable.badge_speed_detector,
                    isCompleted = false,
                    currentProgress = 28,
                    targetProgress = 50,
                    rewardName = "스피드 탐지자 배지"
                ),
                Mission(
                    id = "mission_signal_specialist",
                    title = "신호위반 전문가",
                    description = "신호위반을 100회 신고",
                    iconRes = R.drawable.badge_traffic_expert,
                    isCompleted = false,
                    currentProgress = 67,
                    targetProgress = 100,
                    rewardName = "신호 전문가 배지"
                ),
                Mission(
                    id = "mission_legendary_reporter",
                    title = "전설의 신고자",
                    description = "1000회 신고 달성",
                    iconRes = R.drawable.badge_legendary_reporter,
                    isCompleted = false,
                    currentProgress = 432,
                    targetProgress = 1000,
                    rewardName = "전설의 신고자 배지"
                ),
                Mission(
                    id = "mission_ai_collaborator",
                    title = "AI 협력자",
                    description = "AI 탐지 정확도 향상에 기여",
                    iconRes = R.drawable.badge_ai_collaborator,
                    isCompleted = false,
                    currentProgress = 12,
                    targetProgress = 50,
                    rewardName = "AI 협력자 배지"
                ),
                Mission(
                    id = "mission_city_protector",
                    title = "도시 수호자",
                    description = "우리 동네 교통안전을 크게 개선",
                    iconRes = R.drawable.badge_city_guardian,
                    isCompleted = false,
                    currentProgress = 8,
                    targetProgress = 25,
                    rewardName = "도시 수호자 배지"
                ),
                Mission(
                    id = "mission_pioneer",
                    title = "개척자",
                    description = "새로운 기능을 가장 먼저 사용",
                    iconRes = R.drawable.badge_pioneer,
                    isCompleted = false,
                    currentProgress = 3,
                    targetProgress = 5,
                    rewardName = "개척자 배지"
                )
            )
            Result.success(missionList)
        } catch(e: Exception) {
            Result.failure(e)
        }
    }
}