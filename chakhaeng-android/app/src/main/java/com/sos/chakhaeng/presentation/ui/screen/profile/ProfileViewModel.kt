package com.sos.chakhaeng.presentation.ui.screen.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.R
import com.sos.chakhaeng.core.navigation.Navigator
import com.sos.chakhaeng.core.navigation.Route
import com.sos.chakhaeng.core.session.SessionManager
import com.sos.chakhaeng.domain.model.profile.Badge
import com.sos.chakhaeng.domain.model.profile.Mission
import com.sos.chakhaeng.domain.usecase.profile.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val sessionManager: SessionManager,
    private val navigator: Navigator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshProfile() {
        loadProfile()
    }

    fun showLogoutDialog() {
        _uiState.value = _uiState.value.copy(isLogoutDialogVisible = true)
    }

    fun hideLogoutDialog() {
        _uiState.value = _uiState.value.copy(isLogoutDialogVisible = false)
    }

    fun logout() {
        viewModelScope.launch {
            try {
                sessionManager.logout()
                navigator.navigateAndClearBackStack(Route.Login)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "로그아웃 실패: ${e.message}")
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.loading()

            delay(1000)

            getUserProfileUseCase()
                .onSuccess { userProfile ->

                    val mockBadges = createMockBadges()
                    val mockMissions = createMockMissions()

                    _uiState.value = ProfileUiState.success(
                        userProfile = userProfile,
                        badges = mockBadges,
                        missions = mockMissions
                    )
                }
                .onFailure { error ->
                    Log.e("TAG", "loadProfile: 프로필 데이터 로드 실패: ${error.message}")
                    _uiState.value =  uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
        }
    }

    // Mock 데이터 생성 함수들

    private fun createMockBadges(): List<Badge> {
        return listOf(
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
    }

    private fun createMockMissions(): List<Mission> {
        return listOf(
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
    }
}