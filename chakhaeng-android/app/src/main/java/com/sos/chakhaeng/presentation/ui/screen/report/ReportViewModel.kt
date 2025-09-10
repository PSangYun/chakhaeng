package com.sos.chakhaeng.presentation.ui.screen.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.report.ReportItem
import com.sos.chakhaeng.domain.model.report.ReportStatus
import com.sos.chakhaeng.domain.model.report.ReportTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(

) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * 데이터 로드
     */
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // TODO: 실제 API 호출로 교체
                val mockData = generateMockData()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reportList = mockData,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "데이터를 불러오는 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun selectTab(tab: ReportTab) {
        if (_uiState.value.selectedTab == tab) return

        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    val filteredReportList: StateFlow<List<ReportItem>> = uiState.map { state ->
        when (state.selectedTab) {
            ReportTab.ALL -> state.reportList
            ReportTab.PROCESSING -> state.reportList.filter { it.status == ReportStatus.PROCESSING }
            ReportTab.COMPLETED -> state.reportList.filter { it.status == ReportStatus.COMPLETED }
            ReportTab.REJECTED -> state.reportList.filter { it.status == ReportStatus.REJECTED }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun generateMockData(): List<ReportItem> {
        return listOf(
            ReportItem(
                id = "1",
                violationType = ViolationType.SIGNAL,
                plateNumber = "12가1234",
                location = "강남구 테헤란로 123",
                occurredAt = "2024-01-15 14:30",
                videoFileName = "video_001.mp4",
                status = ReportStatus.PROCESSING,
                createdAt = "2024-01-15 14:30"
            ),
            ReportItem(
                id = "2",
                violationType = ViolationType.LANE,
                plateNumber = "34나5678",
                location = "서초구 서초대로 456",
                occurredAt = "2024-01-15 13:45",
                videoFileName = "video_002.mp4",
                status = ReportStatus.COMPLETED,
                createdAt = "2024-01-15 14:30"
            ),
            ReportItem(
                id = "3",
                violationType = ViolationType.WRONG_WAY,
                plateNumber = "56다9012",
                location = "마포구 월드컵로 789",
                occurredAt = "2024-01-15 12:20",
                videoFileName = "video_003.mp4",
                status = ReportStatus.REJECTED,
                createdAt = "2024-01-15 14:30"
            ),
            ReportItem(
                id = "4",
                violationType = ViolationType.NO_PLATE,
                plateNumber = "78라3456",
                location = "용산구 한강대로 012",
                occurredAt = "2024-01-15 11:15",
                videoFileName = "video_004.mp4",
                status = ReportStatus.PROCESSING,
                createdAt = "2024-01-15 14:30"
            ),
            ReportItem(
                id = "5",
                violationType = ViolationType.SIGNAL,
                plateNumber = "90마7890",
                location = "종로구 종로 345",
                occurredAt = "2024-01-14 16:20",
                videoFileName = "video_005.mp4",
                status = ReportStatus.COMPLETED,
                createdAt = "2024-01-15 14:30"
            ),
            ReportItem(
                id = "6",
                violationType = ViolationType.NO_HELMET,
                plateNumber = "11바2345",
                location = "중구 을지로 678",
                occurredAt = "2024-01-14 10:30",
                videoFileName = "video_006.mp4",
                status = ReportStatus.REJECTED,
                createdAt = "2024-01-15 14:30"
            )
        )
    }
}