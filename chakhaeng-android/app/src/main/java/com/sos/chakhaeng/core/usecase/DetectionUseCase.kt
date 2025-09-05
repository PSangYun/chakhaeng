package com.sos.chakhaeng.core.usecase

import com.sos.chakhaeng.core.utils.DetectionStateManager
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectionUseCase @Inject constructor(
    private val detectionStateManager: DetectionStateManager
) {
    val isDetectionActive: StateFlow<Boolean> = detectionStateManager.isDetectionActive

    fun startDetection() {
        detectionStateManager.startDetection()
    }

    fun stopDetection() {
        detectionStateManager.stopDetection()
    }
}