package com.sos.chakhaeng.presentation.ui.screen.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.navigation.Navigator
import com.sos.chakhaeng.core.navigation.Route
import com.sos.chakhaeng.domain.model.report.ReportItem
import com.sos.chakhaeng.domain.model.report.ReportStatus
import com.sos.chakhaeng.domain.model.report.ReportTab
import com.sos.chakhaeng.domain.usecase.report.DeleteReportItemUseCase
import com.sos.chakhaeng.domain.usecase.report.GetReportItemUseCase
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
    private val navigator: Navigator,
    private val getReportItemUseCase: GetReportItemUseCase,
    private val deleteReportItemUseCase: DeleteReportItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun selectTab(tab: ReportTab) {
        if (_uiState.value.selectedTab == tab) return

        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun loadReportItem() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getReportItemUseCase()
                .onSuccess { reportItems: List<ReportItem> ->
                    _uiState.value = _uiState.value.copy(
                        reportItems = reportItems,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    Log.e("TAG", "loadReportItem error: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
        }
    }

    fun deleteReportItem(reportItem: ReportItem) {
        viewModelScope.launch {
            deleteReportItemUseCase(reportItem.id)
                .onSuccess {
                    Log.d("TAG", "deleteReportItem success - setting snackbar to true")
                    val updatedItems = _uiState.value.reportItems.filter { it.id != reportItem.id }
                    _uiState.value = _uiState.value.copy(
                        reportItems = updatedItems
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = "삭제에 실패했습니다: ${error.message}")
                }
        }
    }

    fun navigateReportDetail(reportId : String){
        viewModelScope.launch {
            navigator.navigate(Route.ReportDetail(reportId))
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    val filteredReportList: StateFlow<List<ReportItem>> = uiState.map { state ->
        when (state.selectedTab) {
            ReportTab.ALL -> state.reportItems
            ReportTab.PROCESSING -> state.reportItems.filter { it.status == ReportStatus.PROCESSING }
            ReportTab.COMPLETED -> state.reportItems.filter { it.status == ReportStatus.COMPLETED }
            ReportTab.REJECTED -> state.reportItems.filter { it.status == ReportStatus.REJECTED }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}