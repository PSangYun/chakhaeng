package com.sos.chakhaeng.presentation.ui.screen.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sos.chakhaeng.presentation.ui.components.report.ReportMediaSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportStatusSection
import androidx.hilt.navigation.compose.hiltViewModel
import com.sos.chakhaeng.presentation.theme.BackgroundGray
import com.sos.chakhaeng.presentation.ui.components.report.LocationInfoSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportContentSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportDetailInfoSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: String,
    reportDetailViewModel: ReportDetailViewModel = hiltViewModel()
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BackgroundGray),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ReportMediaSection(
                videoUrl = uiState.streamingUrl,
            )
        }

        item {
            ReportStatusSection(
                reportDetailItem = uiState.reportDetailItem
            )
        }

        item {
            ReportDetailInfoSection(
                reportDetailItem = uiState.reportDetailItem
            )
        }

        item{
            LocationInfoSection(
                reportDetailItem = uiState.reportDetailItem,
                reportDetailUiState = uiState,
                onLocationRequest = { address ->
                    reportDetailViewModel.getLocationFromAddress(address)
                }
            )
        }

        item {
            ReportContentSection(
                reportDetailItem = uiState.reportDetailItem
            )
        }
    }
}