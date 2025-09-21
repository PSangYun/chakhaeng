package com.sos.chakhaeng.presentation.ui.screen.reportdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReportDetailRoute(
    padding: PaddingValues,
    reportId : String,
    reportDetailViewModel: ReportDetailViewModel = hiltViewModel(),
) {
    val uiState by reportDetailViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        reportDetailViewModel.loadReportDetailItem(reportId)
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            reportDetailViewModel.clearError()
        }
    }


    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
    ) {
        ReportDetailScreen(
            uiState = uiState,
            reportDetailViewModel = reportDetailViewModel
        )
    }
}