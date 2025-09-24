package com.sos.chakhaeng.presentation.ui.screen.mission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.R
import com.sos.chakhaeng.domain.model.profile.Mission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MissionViewModel @Inject constructor(
    // TODO: 실제로는 GetMissionsUseCase 등을 주입받을 예정
) : ViewModel() {

    private val _uiState = MutableStateFlow(MissionUiState())
    val uiState: StateFlow<MissionUiState> = _uiState.asStateFlow()

    init {
        loadMissions()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshMissions() {
        loadMissions()
    }

    private fun loadMissions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // TODO: 실제로는 UseCase를 통해 데이터를 가져올 예정
                val missions = createMockMissions()
                _uiState.value = MissionUiState.success(missions)

            } catch (e: Exception) {
                _uiState.value = MissionUiState.error("미션 정보를 불러오는데 실패했습니다: ${e.message}")
            }
        }
    }

    private fun createMockMissions(): List<Mission> {
        return listOf(
            // 완료된 미션들 (이미 획득한 배지와 매칭)
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
    }
}