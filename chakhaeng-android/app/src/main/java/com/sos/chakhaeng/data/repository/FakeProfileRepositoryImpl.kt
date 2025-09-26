package com.sos.chakhaeng.data.repository

import android.util.Log
import com.sos.chakhaeng.R
import com.sos.chakhaeng.data.mapper.ProfileDataMapper.toEntity
import com.sos.chakhaeng.data.mapper.StatisticsDataMapper.toEntity
import com.sos.chakhaeng.data.network.api.ProfileApi
import com.sos.chakhaeng.data.network.api.StatisticsApi
import com.sos.chakhaeng.domain.model.profile.Badge
import com.sos.chakhaeng.domain.model.profile.Mission
import com.sos.chakhaeng.domain.model.profile.UserProfile
import com.sos.chakhaeng.domain.repository.ProfileRepository
import javax.inject.Inject
import kotlin.Result

class FakeProfileRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi,
    private val statisticsApi: StatisticsApi
) : ProfileRepository {

    override suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val response = profileApi.getUserProfile()
            if (response.success) {
                val userProfile = response.data?.toEntity() ?: UserProfile(
                    name = "",
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

    override suspend fun getUserBadge(): Result<List<Badge>> {
        return try {
            val violationResponse = statisticsApi.getViolationStatistics()
            val reportResponse = statisticsApi.getReportStatistics()
            if (violationResponse.success && reportResponse.success) {
                val violations = violationResponse.data?.totalDetections ?: 0
                val reports = reportResponse.data?.totalReports ?: 0
                val badgeList = listOf(
                    Badge(
                        id = "badge_safety_keeper",
                        name = "교통안전 지킴이",
                        description = "첫 번째 교통위반을 신고하여 획득",
                        iconRes = R.drawable.badge_safety,
                        isUnlocked = violations > 0
                    ),
//                    Badge(
//                        id = "badge_model_citizen",
//                        name = "모범 시민",
//                        description = "정확도 90% 이상을 달성하여 획득",
//                        iconRes = R.drawable.badge_citizen,
//                        isUnlocked = true
//                    ),
                    Badge(
                        id = "badge_detection_master",
                        name = "탐지왕",
                        description = "100회 이상 위반 탐지하여 획득",
                        iconRes = R.drawable.badge_detection,
                        isUnlocked = violations >= 100
                    ),
                    Badge(
                        id = "badge_report_master",
                        name = "신고 마스터",
                        description = "50회 이상 정확한 신고하여 획득",
                        iconRes = R.drawable.badge_report,
                        isUnlocked = reports >= 50
                    ),

                    // 아직 획득하지 못한 배지들 (활동 관련)
                    Badge(
                        id = "badge_attendance_king",
                        name = "연속 출석왕",
                        description = "30일 연속 앱 사용하여 획득",
                        iconRes = R.drawable.badge_daily_king,
                        isUnlocked = reports >= 60
                    ),
//                    Badge(
//                        id = "badge_perfectionist",
//                        name = "완벽주의자",
//                        description = "정확도 95% 이상 달성하여 획득",
//                        iconRes = R.drawable.badge_perfectionist,
//                        isUnlocked = false
//                    ),
//                    Badge(
//                        id = "badge_community_leader",
//                        name = "커뮤니티 리더",
//                        description = "다른 사용자에게 10회 이상 도움을 주어 획득",
//                        iconRes = R.drawable.badge_community_leader,
//                        isUnlocked = false
//                    ),

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
                        isUnlocked = true
                    )
                )
                Result.success(badgeList)
            } else {
                Result.failure(Exception("Badge 불러오는데 실패하였습니다."))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    override suspend fun getRecentCompletedMissions(): Result<List<Mission>> {
        return try {
            val violationResponse = statisticsApi.getViolationStatistics()
            val reportResponse = statisticsApi.getReportStatistics()
            if (violationResponse.success && reportResponse.success) {
                val violations = violationResponse.data?.totalDetections ?: 0
                val reports = reportResponse.data?.totalReports ?: 0
                val missionList = listOf(
                    Mission(
                        id = "mission_first_detection",
                        title = "첫 번째 위반 탐지",
                        description = "교통위반을 1회 탐지",
                        iconRes = R.drawable.badge_safety,
                        isCompleted = violations >= 1,
                        currentProgress = if(violations >=1) 1 else 0,
                        targetProgress = 1,
                        rewardName = "교통안전 지킴이 배지"
                    ),
                    Mission(
                        id = "mission_accurate_reports",
                        title = "정확한 신고",
                        description = "정확한 신고를 5회 완료",
                        iconRes = R.drawable.badge_citizen,
                        isCompleted = reports >= 5,
                        currentProgress = if(reports >=5) 5 else reports,
                        targetProgress = 5,
                        rewardName = "모범 시민 배지"
                    ),
                    Mission(
                        id = "mission_detection_master_100",
                        title = "탐지 마스터",
                        description = "위반을 100회 탐지",
                        iconRes = R.drawable.badge_detection,
                        isCompleted = violations >= 100,
                        currentProgress = if(violations >= 100) 100 else violations,
                        targetProgress = 100,
                        rewardName = "탐지왕 배지"
                    ),
                    Mission(
                        id = "mission_report_master_50",
                        title = "신고 마스터",
                        description = "정확한 신고를 50회 완료",
                        iconRes = R.drawable.badge_report,
                        isCompleted = reports >= 50,
                        currentProgress = if(reports >= 50) 50 else reports,
                        targetProgress = 50,
                        rewardName = "신고 마스터 배지"
                    ),

                    Mission(
                        id = "mission_consecutive_usage",
                        title = "연속 사용",
                        description = "30일 연속으로 앱을 사용",
                        iconRes = R.drawable.badge_daily_king,
                        isCompleted = false,
                        currentProgress = 1,
                        targetProgress = 30,
                        rewardName = "연속 출석왕 배지"
                    ),
                    Mission(
                        id = "mission_night_patrol",
                        title = "야간 순찰",
                        description = "밤 시간대(22:00-06:00)에 100회 탐지",
                        iconRes = R.drawable.badge_night_guardian,
                        isCompleted = false,
                        currentProgress = 0,
                        targetProgress = 100,
                        rewardName = "야간 수호자 배지"
                    ),
                    Mission(
                        id = "mission_weekend_warrior",
                        title = "주말 활동가",
                        description = "주말에 200회 신고",
                        iconRes = R.drawable.badge_weekend_warrior,
                        isCompleted = false,
                        currentProgress = 0,
                        targetProgress = 200,
                        rewardName = "주말 전사 배지"
                    ),
                    Mission(
                        id = "mission_speed_specialist",
                        title = "속도위반 전문가",
                        description = "속도위반을 50회 탐지",
                        iconRes = R.drawable.badge_speed_detector,
                        isCompleted = false,
                        currentProgress = 0,
                        targetProgress = 50,
                        rewardName = "스피드 탐지자 배지"
                    ),
                    Mission(
                        id = "mission_signal_specialist",
                        title = "신호위반 전문가",
                        description = "신호위반을 100회 신고",
                        iconRes = R.drawable.badge_traffic_expert,
                        isCompleted = false,
                        currentProgress = 0,
                        targetProgress = 100,
                        rewardName = "신호 전문가 배지"
                    ),
                    Mission(
                        id = "mission_legendary_reporter",
                        title = "전설의 신고자",
                        description = "1000회 신고 달성",
                        iconRes = R.drawable.badge_legendary_reporter,
                        isCompleted = reports >= 1000,
                        currentProgress = if(reports >= 1000) 1000 else reports,
                        targetProgress = 1000,
                        rewardName = "전설의 신고자 배지"
                    ),
                    Mission(
                        id = "mission_city_protector",
                        title = "도시 수호자",
                        description = "우리 동네 교통안전을 크게 개선",
                        iconRes = R.drawable.badge_city_guardian,
                        isCompleted = reports >= 25,
                        currentProgress = if(reports>=25) 25 else reports,
                        targetProgress = 25,
                        rewardName = "도시 수호자 배지"
                    ),
                    Mission(
                        id = "mission_pioneer",
                        title = "개척자",
                        description = "새로운 기능을 가장 먼저 사용",
                        iconRes = R.drawable.badge_pioneer,
                        isCompleted = false,
                        currentProgress = if(reports >= 1 && violations >=1) 2 else if(reports ==0 && violations ==0) 0 else 1,
                        targetProgress = 5,
                        rewardName = "개척자 배지"
                    )
                )
                Result.success(missionList)
            } else {
                Result.failure(Exception("Badge 불러오는데 실패하였습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllMissions(): Result<List<Mission>> {
        return try {
            val violationResponse = statisticsApi.getViolationStatistics()
            val reportResponse = statisticsApi.getReportStatistics()
            if (violationResponse.success && reportResponse.success) {
                val violations = violationResponse.data?.totalDetections ?: 0
                val reports = reportResponse.data?.totalReports ?: 0
                val missionList = listOf(
                    Mission(
                        id = "mission_first_detection",
                        title = "첫 번째 위반 탐지",
                        description = "교통위반을 1회 탐지",
                        iconRes = R.drawable.badge_safety,
                        isCompleted = violations >= 1,
                        currentProgress = if(violations >=1) 1 else 0,
                        targetProgress = 1,
                        rewardName = "교통안전 지킴이 배지"
                    ),
                    Mission(
                        id = "mission_accurate_reports",
                        title = "정확한 신고",
                        description = "정확한 신고를 5회 완료",
                        iconRes = R.drawable.badge_citizen,
                        isCompleted = reports >= 5,
                        currentProgress = if(reports >=5) 5 else reports,
                        targetProgress = 5,
                        rewardName = "모범 시민 배지"
                    ),
                    Mission(
                        id = "mission_detection_master_100",
                        title = "탐지 마스터",
                        description = "위반을 100회 탐지",
                        iconRes = R.drawable.badge_detection,
                        isCompleted = violations >= 100,
                        currentProgress = if(violations >= 100) 100 else violations,
                        targetProgress = 100,
                        rewardName = "탐지왕 배지"
                    ),
                    Mission(
                        id = "mission_report_master_50",
                        title = "신고 마스터",
                        description = "정확한 신고를 50회 완료",
                        iconRes = R.drawable.badge_report,
                        isCompleted = reports >= 50,
                        currentProgress = if(reports >= 50) 50 else reports,
                        targetProgress = 50,
                        rewardName = "신고 마스터 배지"
                    ),

                    Mission(
                        id = "mission_consecutive_usage",
                        title = "연속 사용",
                        description = "30일 연속으로 앱을 사용",
                        iconRes = R.drawable.badge_daily_king,
                        isCompleted = false,
                        currentProgress = 1,
                        targetProgress = 30,
                        rewardName = "연속 출석왕 배지"
                    ),
                    Mission(
                        id = "mission_night_patrol",
                        title = "야간 순찰",
                        description = "밤 시간대(22:00-06:00)에 100회 탐지",
                        iconRes = R.drawable.badge_night_guardian,
                        isCompleted = false,
                        currentProgress = 0,
                        targetProgress = 100,
                        rewardName = "야간 수호자 배지"
                    ),
                    Mission(
                        id = "mission_weekend_warrior",
                        title = "주말 활동가",
                        description = "주말에 200회 신고",
                        iconRes = R.drawable.badge_weekend_warrior,
                        isCompleted = false,
                        currentProgress = 0,
                        targetProgress = 200,
                        rewardName = "주말 전사 배지"
                    ),
                    Mission(
                        id = "mission_speed_specialist",
                        title = "속도위반 전문가",
                        description = "속도위반을 50회 탐지",
                        iconRes = R.drawable.badge_speed_detector,
                        isCompleted = false,
                        currentProgress = 0,
                        targetProgress = 50,
                        rewardName = "스피드 탐지자 배지"
                    ),
                    Mission(
                        id = "mission_signal_specialist",
                        title = "신호위반 전문가",
                        description = "신호위반을 100회 신고",
                        iconRes = R.drawable.badge_traffic_expert,
                        isCompleted = false,
                        currentProgress = 0,
                        targetProgress = 100,
                        rewardName = "신호 전문가 배지"
                    ),
                    Mission(
                        id = "mission_legendary_reporter",
                        title = "전설의 신고자",
                        description = "1000회 신고 달성",
                        iconRes = R.drawable.badge_legendary_reporter,
                        isCompleted = reports >= 1000,
                        currentProgress = if(reports >= 1000) 1000 else reports,
                        targetProgress = 1000,
                        rewardName = "전설의 신고자 배지"
                    ),
                    Mission(
                        id = "mission_city_protector",
                        title = "도시 수호자",
                        description = "우리 동네 교통안전을 크게 개선",
                        iconRes = R.drawable.badge_city_guardian,
                        isCompleted = reports >= 25,
                        currentProgress = if(reports>=25) 25 else reports,
                        targetProgress = 25,
                        rewardName = "도시 수호자 배지"
                    ),
                    Mission(
                        id = "mission_pioneer",
                        title = "개척자",
                        description = "새로운 기능을 가장 먼저 사용",
                        iconRes = R.drawable.badge_pioneer,
                        isCompleted = false,
                        currentProgress = if(reports >= 1 && violations >=1) 2 else if(reports ==0 && violations ==0) 0 else 1,
                        targetProgress = 5,
                        rewardName = "개척자 배지"
                    )
                )
                Result.success(missionList)
            } else {
                Result.failure(Exception("Badge 불러오는데 실패하였습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}