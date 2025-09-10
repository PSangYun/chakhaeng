package com.sos.chakhaeng.presentation.ui.screen.detection

import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.presentation.model.ViolationDetectionUiModel

data class DetectionUiState(
    val isLoading: Boolean = false,
    val isCameraReady: Boolean = false,
    val isDetectionActive: Boolean = false,
    val isFullscreen: Boolean = false, // 추가
    val cameraPermissionGranted: Boolean = false,
    val error: String? = null,

    // 위반 탐지 관련 상태
    val violationDetections: List<ViolationDetectionUiModel> = emptyList(),
    val selectedViolationFilter: ViolationType = ViolationType.ALL,
    val filteredViolations: List<ViolationDetectionUiModel> = emptyList()
)