package com.sos.chakhaeng.presentation.ui.screen.detection

data class DetectionUiState(
    val isLoading: Boolean = false,
    val isCameraReady: Boolean = false,
    val isDetectionActive: Boolean = false,

    val cameraPermissionGranted: Boolean = false,
    val error: String? = null
)