package com.sos.chakhaeng.presentation.ui.components.detection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sos.chakhaeng.recording.CameraRecordingService

@Composable
fun CameraPreviewSection(
    isDetectionActive: Boolean,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
    onServiceConnected: (CameraRecordingService) -> Unit = {}
) {

    Box(modifier = modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onServiceConnected = onServiceConnected
        )

        CameraStatusOverlay(
            isDetectionActive = isDetectionActive,
            isFullscreen = isFullscreen,
            onToggleFullscreen = onToggleFullscreen,
            modifier = Modifier.fillMaxSize()
        )
    }
}