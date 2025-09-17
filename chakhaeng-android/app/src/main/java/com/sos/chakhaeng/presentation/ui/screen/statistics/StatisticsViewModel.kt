package com.sos.chakhaeng.presentation.ui.screen.statistics

import androidx.lifecycle.ViewModel
import com.sos.chakhaeng.domain.model.statistics.StatisticsTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun selectTab(tab: StatisticsTab) {
        if (_uiState.value.selectedTab == tab) return

        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }


}