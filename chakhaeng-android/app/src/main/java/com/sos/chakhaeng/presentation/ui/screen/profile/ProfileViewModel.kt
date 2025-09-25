package com.sos.chakhaeng.presentation.ui.screen.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.R
import com.sos.chakhaeng.core.navigation.Navigator
import com.sos.chakhaeng.core.navigation.Route
import com.sos.chakhaeng.core.session.SessionManager
import com.sos.chakhaeng.domain.model.profile.Mission
import com.sos.chakhaeng.domain.usecase.profile.GetRecentCompletedMissionsUseCase
import com.sos.chakhaeng.domain.usecase.profile.GetUserBadgeUseCase
import com.sos.chakhaeng.domain.usecase.profile.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserBadgeUseCase: GetUserBadgeUseCase,
    private val getRecentCompletedMissionsUseCase: GetRecentCompletedMissionsUseCase,

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

            getUserProfileUseCase()
                .onSuccess { userProfile ->
                    _uiState.value = _uiState.value.copy(
                        userProfile = userProfile
                    )
                }
                .onFailure { error ->
                    Log.e("TAG", "loadProfile: 사용자 프로필 데이터 로드 실패: ${error.message}")
                    _uiState.value = uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }

            getUserBadgeUseCase()
                .onSuccess { badges ->
                    _uiState.value = _uiState.value.copy(
                        badges = badges
                    )
                }
                .onFailure { error ->
                    Log.e("TAG", "loadProfile: 프로필 배지 데이터 로드 실패: ${error.message}")
                    _uiState.value = uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }

            getRecentCompletedMissionsUseCase()
                .onSuccess { missions ->
                    _uiState.value = _uiState.value.copy(
                        recentCompletedMissions = missions
                    )
                }
                .onFailure { error ->
                    Log.e("TAG", "loadProfile: 프로필 최근 미션 데이터 로드 실패: ${error.message}")
                    _uiState.value = uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }


            _uiState.value = _uiState.value.copy(
                isLoading = false
            )
        }
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