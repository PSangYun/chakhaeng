package com.sos.chakhaeng.presentation.ui.screen.mission

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.*
import com.sos.chakhaeng.presentation.ui.components.mission.EmptyContent
import com.sos.chakhaeng.presentation.ui.components.mission.ErrorContent
import com.sos.chakhaeng.presentation.ui.components.mission.LoadingContent
import com.sos.chakhaeng.presentation.ui.components.mission.MissionItem
import com.sos.chakhaeng.presentation.ui.components.mission.MissionProgressSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionScreen(
    uiState: MissionUiState,
    viewModel: MissionViewModel,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "미션",
                    style = chakhaengTypography().titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "뒤로가기"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = backgroundLight
            )
        )

        // Content
        when {
            uiState.isLoading -> {
                LoadingContent()
            }

            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error,
                    onRetry = {
                        viewModel.clearError()
                        viewModel.refreshMissions()
                    }
                )
            }

            uiState.hasData -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 미션 진행률 섹션
                    item {
                        MissionProgressSection(
                            completedCount = uiState.completedMissionsCount,
                            totalCount = uiState.totalMissionsCount,
                            progressPercentage = uiState.progressPercentage
                        )
                    }

                    item {
                        Text(
                            text = "진행중인 미션",
                            style = chakhaengTypography().titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = onBackgroundLight,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // 활성 미션들
                    items(uiState.activeMissions) { mission ->
                        MissionItem(
                            mission = mission
                        )
                    }

                    // 완료된 미션
                    if (uiState.completedMissions.isNotEmpty()) {
                        item {
                            Text(
                                text = "완료된 미션",
                                style = chakhaengTypography().titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = onBackgroundLight,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(uiState.completedMissions) { mission ->
                            MissionItem(
                                mission = mission,
                            )
                        }
                    }
                }
            }

            else -> {
                EmptyContent()
            }
        }
    }
}