package com.sos.chakhaeng.presentation.ui.screen.allbadges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.R
import com.sos.chakhaeng.domain.model.profile.Badge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllBadgesViewModel @Inject constructor(
    // TODO: 실제로는 GetAllBadgesUseCase, GetUserBadgeProgressUseCase 등을 주입받을 예정
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllBadgesUiState())
    val uiState: StateFlow<AllBadgesUiState> = _uiState.asStateFlow()

    init {
        loadAllBadges()
    }

    fun showBadgeDialog(badge: Badge) {
        _uiState.value = _uiState.value.copy(
            selectedBadge = badge,
            isBadgeDialogVisible = true
        )
    }

    fun hideBadgeDialog() {
        _uiState.value = _uiState.value.copy(
            selectedBadge = null,
            isBadgeDialogVisible = false
        )
    }

    fun refreshBadges() {
        loadAllBadges()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun loadAllBadges() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // TODO: 실제로는 UseCase를 통해 데이터를 가져올 예정
                // val result = getAllBadgesUseCase()
                // result.onSuccess { badges ->
                //     _uiState.value = _uiState.value.copy(
                //         badges = badges,
                //         isLoading = false
                //     )
                // }.onFailure { exception ->
                //     _uiState.value = _uiState.value.copy(
                //         error = "배지 정보를 불러오는데 실패했습니다: ${exception.message}",
                //         isLoading = false
                //     )
                // }

                // 현재는 Mock 데이터 사용
                val allBadges = createAllMockBadges()
                _uiState.value = _uiState.value.copy(
                    badges = allBadges,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "배지 정보를 불러오는데 실패했습니다: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    // Mock 배지 데이터 생성
    private fun createAllMockBadges(): List<Badge> {
        return listOf(
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
    }
}