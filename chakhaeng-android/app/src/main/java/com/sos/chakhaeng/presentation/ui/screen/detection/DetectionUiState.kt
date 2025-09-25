package com.sos.chakhaeng.presentation.ui.screen.detection

import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.violation.ViolationInRangeEntity

data class DetectionUiState(
    val isLoading: Boolean = false,
    val isCameraReady: Boolean = false,
    val isDetectionActive: Boolean = false,
    val isFullscreen: Boolean = false, // 추가
    val cameraPermissionGranted: Boolean = false,
    val error: String? = null,

    val violationDetections: List<ViolationInRangeEntity> = emptyList(),
    val selectedViolationFilter: ViolationType = ViolationType.ALL,
    val filteredViolations: List<ViolationInRangeEntity> = emptyList()
)