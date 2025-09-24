package com.sos.chakhaeng.presentation.ui.screen.mission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack

@Composable
fun MissionRoute(
    navBackStack: NavBackStack,
    viewModel: MissionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MissionScreen(
        uiState = uiState,
        viewModel = viewModel,
        onBackClick = {
            navBackStack.removeLastOrNull()
        }
    )
}