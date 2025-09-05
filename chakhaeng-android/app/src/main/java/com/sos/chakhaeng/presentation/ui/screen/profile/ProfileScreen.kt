package com.sos.chakhaeng.presentation.ui.screen.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun ProfileScreen(
    navigateToStreaming: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "프로필",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.clickable{
                navigateToStreaming()
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${uiState.username} (Lv.${uiState.level})",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "ESG 지수: ${uiState.esgScore}점",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "가챠 포인트: ${uiState.gachaPoints}개",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}