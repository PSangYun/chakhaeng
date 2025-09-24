package com.sos.chakhaeng.presentation.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import com.sos.chakhaeng.core.navigation.Route
import com.sos.chakhaeng.presentation.theme.backgroundLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileRoute(
    padding: PaddingValues,
    navBackStack: NavBackStack,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundLight)
            .padding(padding),
    ) {
        ProfileScreen(
            uiState = uiState,
            viewModel = viewModel,
            onAllBadgesClick = {
                navBackStack.add(Route.AllBadges)
            }
        )
    }
}