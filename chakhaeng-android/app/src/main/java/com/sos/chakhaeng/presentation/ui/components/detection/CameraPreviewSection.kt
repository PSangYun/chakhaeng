package com.sos.chakhaeng.presentation.ui.components.detection

import androidx.camera.core.Camera
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CameraPreviewSection(
    isDetectionActive: Boolean,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onCameraReady: (Camera) -> Unit,
    onImageCaptured: (ImageProxy) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onCameraReady = onCameraReady,
            onImageCaptured = onImageCaptured
        )

        CameraStatusOverlay(
            isDetectionActive = isDetectionActive,
            isFullscreen = isFullscreen,
            onToggleFullscreen = onToggleFullscreen,
            modifier = Modifier.fillMaxSize()
        )
    }
}