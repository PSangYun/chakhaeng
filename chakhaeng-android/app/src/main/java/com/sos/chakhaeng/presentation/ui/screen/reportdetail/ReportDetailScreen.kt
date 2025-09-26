package com.sos.chakhaeng.presentation.ui.screen.reportdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.ui.components.report.ReportMediaSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportStatusSection
import com.sos.chakhaeng.presentation.theme.BackgroundGray
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.ui.components.report.LocationInfoSection
import com.sos.chakhaeng.presentation.ui.components.report.LocationInfoSectionForReportDetail
import com.sos.chakhaeng.presentation.ui.components.report.ReportContentSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportDetailInfoSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(uiState: ReportDetailUiState, reportDetailViewModel: ReportDetailViewModel) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "신고 상세 정보",
                style = chakhaengTypography().titleSmall,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = { reportDetailViewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        ),
        windowInsets = WindowInsets(0)
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BackgroundGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
            LocationInfoSectionForReportDetail(
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