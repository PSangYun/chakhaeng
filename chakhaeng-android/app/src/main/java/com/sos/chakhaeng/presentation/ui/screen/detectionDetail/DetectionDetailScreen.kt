package com.sos.chakhaeng.presentation.ui.screen.detectionDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.R
import com.sos.chakhaeng.presentation.theme.BLUE100
import com.sos.chakhaeng.presentation.theme.BLUE300
import com.sos.chakhaeng.presentation.theme.BackgroundGray
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.ui.components.report.LocationInfoSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportDetailInfoSection
import com.sos.chakhaeng.presentation.ui.components.report.ReportMediaSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionDetailScreen(
    uiState: DetectionDetailUiState,
    detectionDetailViewModel: DetectionDetailViewModel,
    violationId: String
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "위반 상세 정보",
                style = chakhaengTypography().titleSmall,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = { detectionDetailViewModel.navigateBack() }) {
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
            ReportDetailInfoSection(
                reportDetailItem = uiState.reportDetailItem
            )
        }

        item {
            LocationInfoSection(
                reportDetailItem = uiState.reportDetailItem,
                detectionDetailUiState = uiState,
                onLocationRequest = { address ->
                    detectionDetailViewModel.getLocationFromAddress(address)
                }
            )
        }

        item {
            Column {
                Button(
                    onClick = { detectionDetailViewModel.navigateToReportDetail(violationId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BLUE100,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = "신고하러가기",
                        style = chakhaengTypography().titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}