package com.sos.chakhaeng.presentation.ui.screen.detection

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.Camera
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.ai.Detector
import com.sos.chakhaeng.core.navigation.Navigator
import com.sos.chakhaeng.core.navigation.Route
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.usecase.DetectionUseCase
import com.sos.chakhaeng.domain.usecase.ai.ProcessDetectionsUseCase
import com.sos.chakhaeng.presentation.model.ViolationDetectionUiModel
import com.sos.chakhaeng.presentation.mapper.ViolationUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val navigator: Navigator,
    private val detectionUseCase: DetectionUseCase,
    private val detector: Detector,
    private val processDetectionsUseCase: ProcessDetectionsUseCase
) : ViewModel() {

    private val _detections = MutableStateFlow<List<Detection>>(emptyList())
    val detections = _detections.asStateFlow()

    private val _violations = MutableStateFlow<List<ViolationEvent>>(emptyList())
    val violations = _violations.asStateFlow()

    private var lastInferTs = 0L
    private val minGapMs = 100L

    private val _uiState = MutableStateFlow(DetectionUiState())

    val uiState: StateFlow<DetectionUiState> = combine(
        _uiState,
        detectionUseCase.isDetectionActive
    ) { state, isDetectionActive ->
        val filteredViolations = filterViolations(
            violations = state.violationDetections,
            filter = state.selectedViolationFilter
        )

        state.copy(
            isDetectionActive = isDetectionActive,
            filteredViolations = filteredViolations
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetectionUiState()
    )

    private var camera: Camera? = null

    init {
        viewModelScope.launch {
            viewModelScope.launch(Dispatchers.Default) { detector.warmup() }

            detectionUseCase.isDetectionActive.collect { isActive ->
                if (isActive) {
                    initializeCamera()
                    generateSampleViolationData()
                } else {
                    pauseCamera()
                }
            }
        }
    }

    fun onFrame(bitmap: Bitmap, rotation: Int) {
        val now = System.currentTimeMillis()
        if (now - lastInferTs < minGapMs) return
        lastInferTs = now

        viewModelScope.launch(Dispatchers.Default) {
            val dets = detector.detect(bitmap, rotation)
            _detections.value = dets
            _violations.value = processDetectionsUseCase(dets)
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

    fun navigateViolationDetail(violationId : String?){
        viewModelScope.launch {
            navigator.navigate(Route.ViolationDetail(violationId))
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
    }

    fun toggleFullscreen() {
        _uiState.value = _uiState.value.copy(
            isFullscreen = !_uiState.value.isFullscreen
        )
    }

    fun onViolationFilterSelected(filter: ViolationType) {
        _uiState.value = _uiState.value.copy(selectedViolationFilter = filter)
    }

    fun onViolationClick(violation: ViolationDetectionUiModel) {
        Log.d("DetectionViewModel", "위반 선택됨: ${violation.id}")
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun filterViolations(
        violations: List<ViolationDetectionUiModel>,
        filter: ViolationType
    ): List<ViolationDetectionUiModel> {
        return if (filter == ViolationType.ALL) {
            violations
        } else {
            violations.filter { it.type == filter }
        }
    }

    private fun generateSampleViolationData() {
        val sampleViolations = listOf(
            ViolationUiMapper.mapToUiModel(
                id = "1",
                type = ViolationType.WRONG_WAY,
                licenseNumber = "12가1234",
                location = "강남구 테헤란로 123",
                detectedAt = LocalDateTime.now().minusMinutes(2),
                confidence = 0.95f,
                thumbnailUrl = null
            ),
            ViolationUiMapper.mapToUiModel(
                id ="1",
                type = ViolationType.SIGNAL,
                licenseNumber = "34나5678",
                location = "서초구 서초대로 456",
                detectedAt = LocalDateTime.now().minusMinutes(5),
                confidence = 0.88f,
                thumbnailUrl = null
            ),
            ViolationUiMapper.mapToUiModel(
                id ="1",
                type = ViolationType.LANE,
                licenseNumber = "56다9012",
                location = "마포구 월드컵로 789",
                detectedAt = LocalDateTime.now().minusMinutes(12),
                confidence = 0.73f,
                thumbnailUrl = null
            ),
            ViolationUiMapper.mapToUiModel(
                id = "1",
                type = ViolationType.NO_PLATE,
                licenseNumber = "78라3456",
                location = "용산구 한강대로 321",
                detectedAt = LocalDateTime.now().minusMinutes(25),
                confidence = 0.82f,
                thumbnailUrl = null
            )
        )

        _uiState.value = _uiState.value.copy(violationDetections = sampleViolations)
    }

    override fun onCleared() {
        detector.close()
        super.onCleared()
        camera = null
    }
}