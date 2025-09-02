package com.sos.chakhaeng.presentation.ui.screen.detection

data class DetectionUiState(
    val isDetecting: Boolean = false,
    val detectedObjects: List<String> = emptyList(),
    val cameraPermissionGranted: Boolean = false
)