package com.sos.chakhaeng.presentation.ui.screen.detection

import android.Manifest
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.sos.chakhaeng.presentation.ui.components.detection.CameraPreview

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
                        onCameraReady = { camera ->
                            viewModel.onCameraReady(camera)
                        },
                        onImageCaptured = { imageProxy ->
                            viewModel.processFrame(imageProxy)
                        }
                    )
                }

                // 탐지가 비활성화된 경우 오버레이
                !uiState.isDetectionActive -> {
                    CameraInactiveOverlay()
                }

                uiState.error != null -> {
                    CameraErrorScreen(
                        error = uiState.error!!,
                        onRetry = {
                            viewModel.clearError()
                        }
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

@Composable
private fun CameraPreviewSection(
    isDetectionActive: Boolean,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onCameraReady: (Camera) -> Unit,
    onImageCaptured: (ImageProxy) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onCameraReady = onCameraReady,
            onImageCaptured = onImageCaptured
        )

        CameraOverlay(
            isDetectionActive = isDetectionActive,
            isFullscreen = isFullscreen,
            onToggleFullscreen = onToggleFullscreen,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CameraOverlay(
    isDetectionActive: Boolean,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (isDetectionActive) Color.Green else Color.Gray,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isDetectionActive) "탐지 중" else "대기 중",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 전체화면/최소화 버튼
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.6f)
            )
        ) {
            IconButton(
                onClick = onToggleFullscreen,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isFullscreen) {
                        Icons.Default.FullscreenExit
                    } else {
                        Icons.Default.Fullscreen
                    },
                    contentDescription = if (isFullscreen) "최소화" else "전체화면",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// 카메라 비활성화 시 보여줄 오버레이
@Composable
private fun CameraInactiveOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = "홈에서 탐지를 시작해주세요",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// 권한 요청 화면
@Composable
private fun CameraPermissionRequest(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "카메라 권한이 필요합니다",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "교통위반 탐지를 위해 카메라 접근 권한을 허용해주세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRequestPermission) {
                Text("권한 허용")
            }
        }
    }
}

// 로딩 화면
@Composable
private fun CameraLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color.White
            )
            Text(
                text = "카메라를 준비하고 있습니다...",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// 에러 화면
@Composable
private fun CameraErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "카메라 오류",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text("다시 시도")
            }
        }
    }
}