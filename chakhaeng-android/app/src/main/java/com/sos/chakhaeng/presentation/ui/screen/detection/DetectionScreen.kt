package com.sos.chakhaeng.presentation.ui.screen.detection

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.sos.chakhaeng.presentation.model.ViolationDetectionUiModel
import com.sos.chakhaeng.presentation.theme.onPrimaryContainerLight
import com.sos.chakhaeng.presentation.theme.onPrimaryLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.ui.components.detection.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel = hiltViewModel(),
    onCreateNewViolation: () -> Unit,
    // id 생기면 대체
//    onViolationClick: (Long) -> Unit,
    onViolationClick: (ViolationDetectionUiModel) -> Unit,
    paddingValues: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    DisposableEffect(Unit) {
        onDispose {
            viewModel.pauseCamera()
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(if (uiState.isFullscreen) 1f else 0.3f)
            ) {
                when {
                    !cameraPermission.status.isGranted -> {
                        CameraPermissionRequest(
                            onRequestPermission = { cameraPermission.launchPermissionRequest() }
                        )
                    }
                    uiState.isLoading -> CameraLoadingScreen()
                    uiState.isDetectionActive && uiState.isCameraReady -> {
                        CameraPreviewSection(
                            isDetectionActive = uiState.isDetectionActive,
                            isFullscreen = uiState.isFullscreen,
                            onToggleFullscreen = viewModel::toggleFullscreen,
                            onCameraReady = viewModel::onCameraReady
                        )
                    }
                    !uiState.isDetectionActive -> CameraInactiveOverlay()
                    uiState.error != null -> {
                        CameraErrorScreen(
                            error = uiState.error!!,
                            onRetry = viewModel::clearError
                        )
                    }
                }
            }

            // 하단 위반 목록
            if (!uiState.isFullscreen) {
                ViolationDetectionSection(
                    selectedFilter = uiState.selectedViolationFilter,
                    violations = uiState.filteredViolations,
                    onFilterSelected = viewModel::onViolationFilterSelected,
                    onViolationClick = onViolationClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
        }
        FloatingActionButton(
            onClick = onCreateNewViolation,
            containerColor = primaryLight,
            contentColor = onPrimaryLight,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "새로 만들기")
        }
    }

}
