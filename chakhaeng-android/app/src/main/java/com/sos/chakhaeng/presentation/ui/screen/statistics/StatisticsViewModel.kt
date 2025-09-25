package com.sos.chakhaeng.presentation.ui.screen.statistics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.domain.model.statistics.StatisticsTab
import com.sos.chakhaeng.domain.usecase.statistics.GetReportStatisticsUseCase
import com.sos.chakhaeng.domain.usecase.statistics.GetViolationStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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

    private val TAG = "StatisticsVM"

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.loading()

            try {
                // 둘 다 병렬로 요청
                val (violationResult, reportResult) = kotlinx.coroutines.coroutineScope {
                    val v = async { getViolationStatisticsUseCase() }   // Result<ViolationStatistics>
                    val r = async { getReportStatisticsUseCase() }      // Result<ReportStatistics>
                    v.await() to r.await()
                }

                val violation = violationResult.getOrNull()
                val report = reportResult.getOrNull()

                if (violation != null || report != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        violationStats = violation ?: _uiState.value.violationStats,
                        reportStats = report ?: _uiState.value.reportStats,
                        error = null
                    )
                } else {
                    // 둘 다 실패
                    val vErr = violationResult.exceptionOrNull()
                    val rErr = reportResult.exceptionOrNull()
                    val msg = buildString {
                        append("통계 데이터 로드 실패.")
                        vErr?.message?.let { append(" [Violation] ").append(it) }
                        rErr?.message?.let { append(" [Report] ").append(it) }
                    }.ifBlank { "통계 데이터 로드 실패: 알 수 없는 오류" }

                    // 전체 스택트레이스로 로깅 (cause 말고 e 자체를 로그로)
                    Log.e(TAG, msg, vErr ?: rErr)
                    _uiState.value = StatisticsUiState.error(msg)
                }
            } catch (e: Throwable) {
                // e.cause는 종종 null 입니다 → e 자체를 로그로 남기세요
                Log.e(TAG, "통계 데이터 로드 중 예외", e)
                _uiState.value = StatisticsUiState.error(
                    "통계 데이터 로드 실패: ${e.message ?: e::class.simpleName}"
                )
            }
        }
    }


    fun refreshStatistics() {
        loadStatistics()
    }

}