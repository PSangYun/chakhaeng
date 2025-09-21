package com.sos.chakhaeng.presentation.ui.screen.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.sos.chakhaeng.domain.model.report.ReportItem
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.ui.components.report.EmptySection
import com.sos.chakhaeng.presentation.ui.components.report.LoadingSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportListSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportTabSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    uiState: ReportUiState,
    reportViewModel: ReportViewModel,
    filteredReportList: List<ReportItem>
) {
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
            ),
            windowInsets = WindowInsets(0)
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
                    reportViewModel.navigateReportDetail(reportItem.id)
                },
                onDelete = { reportItem ->
                    reportViewModel.deleteReportItem(reportItem)
                }
            )
        }

    }
}