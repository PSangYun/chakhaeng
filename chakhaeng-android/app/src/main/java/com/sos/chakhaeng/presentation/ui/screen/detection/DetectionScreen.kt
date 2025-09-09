package com.sos.chakhaeng.presentation.ui.screen.detection

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.sos.chakhaeng.presentation.ui.components.detection.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel = hiltViewModel()
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

    Column(modifier = Modifier.fillMaxSize()) {
        // 카메라 영역
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
                onViolationClick = viewModel::onViolationClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
        }
    }
}
