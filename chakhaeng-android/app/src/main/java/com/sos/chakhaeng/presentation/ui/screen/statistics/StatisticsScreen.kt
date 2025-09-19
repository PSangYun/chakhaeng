package com.sos.chakhaeng.presentation.ui.screen.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sos.chakhaeng.presentation.theme.BackgroundGray
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.ui.components.statistics.section.ErrorSection
import com.sos.chakhaeng.presentation.ui.components.statistics.section.LoadingSection
import com.sos.chakhaeng.presentation.ui.components.statistics.StatisticsContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    statisticsViewModel: StatisticsViewModel= hiltViewModel(),
    paddingValues: PaddingValues
) {
    val uiState by statisticsViewModel.uiState.collectAsStateWithLifecycle()

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            statisticsViewModel.clearError()
        }
    }
     val bottomPadding = paddingValues.calculateBottomPadding()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(bottom = bottomPadding)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "통계 현황",
                    style = chakhaengTypography().titleSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        when {
            uiState.isLoading -> {
                LoadingSection()
            }
            uiState.error != null -> {
                ErrorSection(
                    error = uiState.error!!,
                    onRetry = { statisticsViewModel.refreshStatistics() }
                )
            }
            else -> {
                StatisticsContent(
                    uiState = uiState,
                    onTabSelected = statisticsViewModel::selectTab
                )
            }
        }
    }
}
