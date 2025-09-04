package com.sos.chakhaeng.core.usecase

import com.sos.chakhaeng.core.repository.DetectionStateRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectionUseCase @Inject constructor(
    private val detectionStateRepository: DetectionStateRepository
) {
    val isDetectionActive: StateFlow<Boolean> = detectionStateRepository.isDetectionActive

    fun startDetection() {
        detectionStateRepository.startDetection()
    }

    fun stopDetection() {
        detectionStateRepository.stopDetection()
    }
}