package com.sos.chakhaeng.presentation.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sos.chakhaeng.presentation.ui.components.home.DetectionSection
import com.sos.chakhaeng.presentation.ui.components.home.ESGScoreCard
import com.sos.chakhaeng.presentation.ui.components.home.RecentViolationsSection
import com.sos.chakhaeng.presentation.ui.theme.primaryLight
import com.sos.chakhaeng.presentation.ui.model.TodayInfoUiModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetection: () -> Unit = {},
    paddingValues: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                    viewModel.dismissDetectionDialog()
                    viewModel.startDetection()
                    onNavigateToDetection()
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
                .padding(bottom = 16.dp),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp
                        )
                    )
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = paddingValues
            ) {
                // ESG 점수 카드
                item {
                    ESGScoreCard(
                        score = uiState.esgScore,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 오늘의 탐지/신고 현황
                item {
                    TodayStatsRow(
                        todayInfo = uiState.todayInfo,
                        modifier = Modifier
                    )
                }

                // 실시간 위반 감지 리스트
                item {
                    RecentViolationsSection(
                        violations = uiState.recentViolations,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

}


@Composable
private fun TodayStatsRow(
    todayInfo: TodayInfoUiModel,
    modifier: Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 오늘 탐지 횟수
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${todayInfo.todayDetectionCount}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
                Text(
                    text = "오늘 탐지",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 신고 완료 횟수
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Report,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${todayInfo.todayReportCount}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
                Text(
                    text = "신고 완료",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
