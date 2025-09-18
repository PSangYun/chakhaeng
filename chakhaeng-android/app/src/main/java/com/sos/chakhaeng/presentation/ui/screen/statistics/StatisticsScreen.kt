package com.sos.chakhaeng.presentation.ui.screen.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatisticsScreen(uiState: StatisticsUiState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "통계",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "총 탐지: ${uiState.totalDetections}회",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "정확도: ${uiState.accuracy}%",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
