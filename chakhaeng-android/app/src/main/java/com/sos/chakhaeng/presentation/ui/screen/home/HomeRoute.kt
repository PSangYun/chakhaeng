package com.sos.chakhaeng.presentation.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sos.chakhaeng.presentation.theme.primaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeRoute(
    padding: PaddingValues,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
        viewModel.loadTodayStats()
        viewModel.loadRecentViolation()
    }


    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(if (uiState.isDetectionActive) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else primaryLight)
                .padding(padding)
    ) {
        HomeScreen(
            uiState = uiState,
            viewModel = viewModel
        )
    }
}