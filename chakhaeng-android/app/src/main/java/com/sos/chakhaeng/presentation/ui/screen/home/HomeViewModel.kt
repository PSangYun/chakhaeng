package com.sos.chakhaeng.presentation.ui.screen.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.navigation.BottomTabRoute
import com.sos.chakhaeng.core.navigation.Navigator
import com.sos.chakhaeng.core.navigation.Route
import com.sos.chakhaeng.core.utils.DetectionStateManager
import com.sos.chakhaeng.domain.model.home.RecentViolation
import com.sos.chakhaeng.domain.model.home.TodayStats
import com.sos.chakhaeng.domain.usecase.home.GetRecentViolationUseCase
import com.sos.chakhaeng.domain.usecase.home.GetTodayStatsUseCase
import com.sos.chakhaeng.recording.startCameraFgService
import com.sos.chakhaeng.recording.stopCameraFgService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val app: Context,
    private val navigator: Navigator,
    private val detectionStateManager: DetectionStateManager,
    private val getTodayStatsUseCase: GetTodayStatsUseCase,
    private val getRecentViolationUseCase: GetRecentViolationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())

    // DetectionStateManager의 상태와 UI 상태를 결합 -> 카메라 샹태를 전역으로 관리하기 위해
    val uiState: StateFlow<HomeUiState> = combine(
        _uiState,
        detectionStateManager.isDetectionActive
    ) { state, isDetectionActive ->
        state.copy(isDetectionActive = isDetectionActive)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun navigateDetection(){
        viewModelScope.launch {
            navigator.navigate(BottomTabRoute.Detect)
        }
    }
    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                esgScore = 1250
            )
            Log.d(TAG, "loadHomeData: recentViolations 아이템 목록: ${uiState.value.recentViolations}")
        }
    }

    // 탐지 시작 다이얼로그 표시
    fun showDetectionStartDialog() {
        _uiState.value = _uiState.value.copy(showDetectionDialog = true)
    }

    // 탐지 시작 다이얼로그 닫기
    fun dismissDetectionDialog() {
        _uiState.value = _uiState.value.copy(showDetectionDialog = false)
    }

    // 탐지 시작
    fun startDetection() {
        detectionStateManager.startDetection()
        app.startCameraFgService()
        Log.d(TAG, "탐지 시작됨")
    }

    // 탐지 정지 다이얼로그 표시
    fun showStopDetectionDialog() {
        _uiState.value = _uiState.value.copy(showStopDetectionDialog = true)
    }

    // 탐지 정지 다이얼로그 닫기
    fun dismissStopDetectionDialog() {
        _uiState.value = _uiState.value.copy(showStopDetectionDialog = false)
    }

    // 탐지 정지 확인
    fun confirmStopDetection() {
        detectionStateManager.stopDetection()
        _uiState.value = _uiState.value.copy(showStopDetectionDialog = false)
        Log.d(TAG, "탐지 정지됨")
        app.stopCameraFgService()
    }

    fun loadTodayStats() {
        viewModelScope.launch {
            getTodayStatsUseCase()
                .onSuccess { todayStats: TodayStats ->
                    _uiState.value = _uiState.value.copy(todayStats = todayStats)
                }
                .onFailure { /* handle error */ }
        }
    }

    fun loadRecentViolation() {
        viewModelScope.launch {
            getRecentViolationUseCase()
                .onSuccess { recentViolations: List<RecentViolation> ->
                    _uiState.value = _uiState.value.copy(recentViolations = recentViolations)
                }
                .onFailure { /* handle error */ }
        }
    }

    fun navigateDetectionDetail(violationId : String){
        viewModelScope.launch {
            navigator.navigate(Route.DetectionDetail(violationId))
        }
    }
}