package com.sos.chakhaeng.presentation.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.neutral
import com.sos.chakhaeng.presentation.ui.components.home.DetectionSection
import com.sos.chakhaeng.presentation.ui.components.home.RecentViolationsSection
import com.sos.chakhaeng.presentation.ui.components.home.TodayState
import com.sos.chakhaeng.presentation.theme.primaryLight

@Composable
fun HomeScreen(uiState: HomeUiState, viewModel: HomeViewModel) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 위반 탐지 섹션
        DetectionSection(
            isDetectionActive = uiState.isDetectionActive,
            onDetectionAction = {
                if (uiState.isDetectionActive) {
                    viewModel.showStopDetectionDialog()
                } else {
                    viewModel.showDetectionStartDialog()
                }
            },
            showDialog = uiState.showDetectionDialog || uiState.showStopDetectionDialog,
            onDialogConfirm = {
                if (uiState.isDetectionActive) {
                    viewModel.confirmStopDetection()
                } else {
                    viewModel.navigateDetection()
                    viewModel.dismissDetectionDialog()
                    viewModel.startDetection()
                }
            },
            onDialogDismiss = {
                if (uiState.isDetectionActive) {
                    viewModel.dismissStopDetectionDialog()
                } else {
                    viewModel.dismissDetectionDialog()
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = if (uiState.isDetectionActive) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else primaryLight
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp
                        )
                    )
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 오늘의 탐지/신고 현황
                item {
                    TodayState(
                        todayStats = uiState.todayStats,
                        modifier = Modifier
                    )
                }
                item{
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 2.dp,
                        color = neutral
                    )
                }
                // 실시간 위반 감지 리스트
                item {
                    RecentViolationsSection(
                        violations = uiState.recentViolations
                    )
                }
            }
        }
    }
}