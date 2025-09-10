package com.sos.chakhaeng.presentation.ui.screen.report

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
import com.sos.chakhaeng.presentation.theme.BackgroundGray
import com.sos.chakhaeng.presentation.theme.backgroundLight
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.ui.components.report.EmptySection
import com.sos.chakhaeng.presentation.ui.components.report.LoadingSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportListSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportTabSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel(),
    paddingValues: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredReportList by viewModel.filteredReportList.collectAsStateWithLifecycle()

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
            onTabSelected = viewModel::selectTab,
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
                paddingValues = paddingValues
            )
        }
    }
}