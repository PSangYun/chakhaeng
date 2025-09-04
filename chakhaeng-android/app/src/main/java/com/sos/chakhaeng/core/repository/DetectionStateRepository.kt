package com.sos.chakhaeng.core.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectionStateRepository @Inject constructor() {
    private val _isDetectionActive = MutableStateFlow(false)
    val isDetectionActive: StateFlow<Boolean> = _isDetectionActive.asStateFlow()

    fun startDetection() {
        _isDetectionActive.value = true
    }

    fun stopDetection() {
        _isDetectionActive.value = false
    }
}