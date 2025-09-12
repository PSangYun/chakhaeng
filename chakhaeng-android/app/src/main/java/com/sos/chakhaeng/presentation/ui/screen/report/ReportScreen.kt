package com.sos.chakhaeng.presentation.ui.screen.report

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.ui.components.report.EmptySection
import com.sos.chakhaeng.presentation.ui.components.report.LoadingSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportListSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportTabSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    reportViewModel: ReportViewModel = hiltViewModel(),
    paddingValues: PaddingValues
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "신고 현황",
                    style = chakhaengTypography().titleSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        ReportTabSection(
            selectedTab = uiState.selectedTab,
            onTabSelected = reportViewModel::selectTab,
            modifier = Modifier
                .fillMaxWidth()
        )

        if (uiState.isLoading) {
            LoadingSection()
        } else if (filteredReportList.isEmpty()) {
            EmptySection(selectedTab = uiState.selectedTab)
        } else {
            ReportListSection(
                reportList = filteredReportList,
                modifier = Modifier
                    .fillMaxSize(),
                onItemClick = { reportItem ->
                    // TODO: 상세 화면으로 이동
                },
                onDelete = { reportItem ->
                    reportViewModel.deleteReportItem(reportItem)
                }
            )
        }

    }
}