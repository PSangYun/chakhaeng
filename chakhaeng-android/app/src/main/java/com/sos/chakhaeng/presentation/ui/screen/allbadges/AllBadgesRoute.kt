package com.sos.chakhaeng.presentation.ui.screen.allbadges

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack

@Composable
fun AllBadgesRoute(
    navBackStack: NavBackStack,
    viewModel: AllBadgesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AllBadgesScreen(
        uiState = uiState,
        viewModel = viewModel,
        onBackClick = {
            navBackStack.removeLastOrNull()
        }
    )
}