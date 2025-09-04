package com.sos.chakhaeng.presentation.ui.screen.detection

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.usecase.DetectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val detectionUseCase: DetectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetectionUiState())

    val uiState: StateFlow<DetectionUiState> = combine(
        _uiState,
        detectionUseCase.isDetectionActive
    ) { state, isDetectionActive ->
        state.copy(isDetectionActive = isDetectionActive)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetectionUiState()
    )

    private var camera: Camera? = null

    init {
        viewModelScope.launch {
            detectionUseCase.isDetectionActive.collect { isActive ->
                if (isActive) {
                    initializeCamera()
                } else {
                    pauseCamera()
                }
            }
        }
    }

    fun initializeCamera() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCameraReady = true,
                    cameraPermissionGranted = true
                )
                Log.d("DetectionViewModel", "카메라 초기화 완료")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "카메라 초기화 실패: ${e.message}"
                )
            }
        }
    }

    fun onCameraReady(camera: Camera) {
        this.camera = camera
        Log.d("DetectionViewModel", "카메라 준비 완료")
    }

    fun pauseCamera() {
        _uiState.value = _uiState.value.copy(
            isCameraReady = false,
            isLoading = false
        )
        Log.d("DetectionViewModel", "카메라 정지됨")
    }

    fun processFrame(imageProxy: ImageProxy) {
        if (uiState.value.isDetectionActive && uiState.value.isCameraReady) {
            Log.d("DetectionViewModel", "프레임 처리 중: ${imageProxy.width}x${imageProxy.height}")
        }
        imageProxy.close()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        camera = null
    }
}