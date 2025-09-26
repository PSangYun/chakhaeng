package com.sos.chakhaeng.presentation.ui.screen.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReportRoute(
    padding: PaddingValues,
    reportViewModel: ReportViewModel = hiltViewModel(),
) {
    val uiState by reportViewModel.uiState.collectAsStateWithLifecycle()
    val filteredReportList by reportViewModel.filteredReportList.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        reportViewModel.loadReportItem()
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            reportViewModel.clearError()
        }
    }


    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { reportViewModel.loadReportItem() },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(padding)
    ) {
        ReportScreen(
            uiState = uiState,
            filteredReportList = filteredReportList,
            reportViewModel = reportViewModel
        )
    }
}