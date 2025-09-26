package com.sos.chakhaeng.presentation.ui.screen.detectionDetail

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
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetectionDetailRoute(
    padding: PaddingValues,
    violationId : String,
    detectionDetailViewModel: DetectionDetailViewModel = hiltViewModel(),
) {
    val uiState by detectionDetailViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        detectionDetailViewModel.loadDetectDetailItem(violationId)
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            detectionDetailViewModel.clearError()
        }
    }


    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
    ) {
        DetectionDetailScreen(
            violationId =violationId,
            uiState = uiState,
            detectionDetailViewModel = detectionDetailViewModel
        )
    }
}