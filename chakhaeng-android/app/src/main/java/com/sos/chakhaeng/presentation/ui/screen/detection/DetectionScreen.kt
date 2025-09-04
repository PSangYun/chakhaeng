package com.sos.chakhaeng.presentation.ui.screen.detection

import android.Manifest
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.sos.chakhaeng.presentation.ui.components.detection.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isFullscreen by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("DetectionScreen", "DetectionScreen disposed - 카메라 정리")
            viewModel.pauseCamera()
        }
    }

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        when {
            cameraPermission.status.isGranted -> {
                // 권한이 있으면 ViewModel에서 자동으로 상태를 감지
            }
            cameraPermission.status.shouldShowRationale -> {
                // 권한 설명
            }
            else -> {
                cameraPermission.launchPermissionRequest()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(if (isFullscreen) 1f else 0.3f)
        ) {
            when {
                !cameraPermission.status.isGranted -> {
                    CameraPermissionRequest(
                        onRequestPermission = {
                            cameraPermission.launchPermissionRequest()
                        }
                    )
                }

                uiState.isLoading -> {
                    CameraLoadingScreen()
                }

                // 탐지 활성화되고 카메라 준비된 경우만 카메라 프리뷰
                uiState.isDetectionActive && uiState.isCameraReady -> {
                    CameraPreviewSection(
                        isDetectionActive = uiState.isDetectionActive,
                        isFullscreen = isFullscreen,
                        onToggleFullscreen = { isFullscreen = !isFullscreen },
                        onCameraReady = viewModel::onCameraReady,
                        onImageCaptured = viewModel::processFrame
                    )
                }

                // 탐지가 비활성화된 경우 오버레이
                !uiState.isDetectionActive -> {
                    CameraInactiveOverlay()
                }

                uiState.error != null -> {
                    CameraErrorScreen(
                        error = uiState.error!!,
                        onRetry = viewModel::clearError
                    )
                }
            }
        }

        if (!isFullscreen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "하단 영역",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
