package com.sos.chakhaeng.core.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DetectionStateManager {
    private val _isDetectionActive = MutableStateFlow(false)
    val isDetectionActive: StateFlow<Boolean> = _isDetectionActive.asStateFlow()

    fun startDetection() {
        _isDetectionActive.value = true
    }

    fun stopDetection() {
        _isDetectionActive.value = false
    }
}