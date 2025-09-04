package com.sos.chakhaeng.presentation.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.utils.DetectionStateManager
import com.sos.chakhaeng.presentation.ui.model.RecentViolationUiModel
import com.sos.chakhaeng.presentation.ui.model.ViolationSeverity
import com.sos.chakhaeng.presentation.ui.model.TodayInfoUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())

    // DetectionStateManager의 상태와 UI 상태를 결합 -> 카메라 샹태를 전역으로 관리하기 위해
    val uiState: StateFlow<HomeUiState> = combine(
        _uiState,
        DetectionStateManager.isDetectionActive
    ) { state, isDetectionActive ->
        state.copy(isDetectionActive = isDetectionActive)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                esgScore = 1250,
                todayInfo = TodayInfoUiModel(
                    todayDetectionCount = 8,
                    todayReportCount = 3
                ),
                recentViolations = getMockRecentViolations()
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
        DetectionStateManager.startDetection()
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
        DetectionStateManager.stopDetection()
        _uiState.value = _uiState.value.copy(showStopDetectionDialog = false)
        Log.d(TAG, "탐지 정지됨")
    }

    private fun getMockRecentViolations(): List<RecentViolationUiModel> {
        return listOf(
            RecentViolationUiModel(
                id = 1,
                type = "신호위반",
                location = "강남구 테헤란로",
                timestamp = System.currentTimeMillis() - 1200000,
                carNumber = "12가1234",
                severity = ViolationSeverity.HIGH
            ),
            RecentViolationUiModel(
                id = 2,
                type = "차선침범",
                location = "서초구 서초대로",
                timestamp = System.currentTimeMillis() - 3600000,
                carNumber = "34나5678",
                severity = ViolationSeverity.MEDIUM
            ),
            RecentViolationUiModel(
                id = 3,
                type = "역주행",
                location = "마포구 월드컵로",
                timestamp = System.currentTimeMillis() - 7200000,
                carNumber = "56다9012",
                severity = ViolationSeverity.CRITICAL
            )
        )
    }
}