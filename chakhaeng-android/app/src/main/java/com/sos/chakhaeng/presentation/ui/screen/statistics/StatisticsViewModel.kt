package com.sos.chakhaeng.presentation.ui.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.domain.model.statistics.StatisticsTab
import com.sos.chakhaeng.domain.usecase.statistics.GetReportStatisticsUseCase
import com.sos.chakhaeng.domain.usecase.statistics.GetViolationStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getReportStatisticsUseCase: GetReportStatisticsUseCase,
    private val getViolationStatisticsUseCase: GetViolationStatisticsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun selectTab(tab: StatisticsTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.loading()

            try {
                val violationResult = getViolationStatisticsUseCase()
                val reportResult = getReportStatisticsUseCase()

                if (violationResult.isSuccess && reportResult.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        violationStats = violationResult.getOrThrow(),
                        reportStats = reportResult.getOrThrow()
                    )
                } else {
                    val errorMessage = violationResult.exceptionOrNull()?.message
                        ?: reportResult.exceptionOrNull()?.message
                        ?: "알 수 없는 오류"
                    _uiState.value = StatisticsUiState.error("통계 데이터 로드 실패: $errorMessage")
                }
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.error("통계 데이터 로드 실패: ${e.message}")
            }
        }
    }

    fun refreshStatistics() {
        loadStatistics()
    }

}