package com.sos.chakhaeng.presentation.ui.screen.detection

import android.util.Log
import androidx.camera.core.Camera
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.ai.Detector
import com.sos.chakhaeng.core.ai.MultiModelInterpreterDetector
import com.sos.chakhaeng.core.navigation.Navigator
import com.sos.chakhaeng.core.navigation.Route
import com.sos.chakhaeng.core.utils.DetectionSessionHolder
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.model.violation.ViolationInRangeEntity
import com.sos.chakhaeng.domain.usecase.violation.DetectionUseCase
import com.sos.chakhaeng.domain.usecase.violation.GetViolationsInRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val navigator: Navigator,
    private val detectionUseCase: DetectionUseCase,
    private val getViolationsInRangeUseCase: GetViolationsInRangeUseCase,
    private val detector: Detector,
    private val sessionHolder: DetectionSessionHolder
) : ViewModel() {

    private val _detections = MutableStateFlow<List<Detection>>(emptyList())
    val detections: StateFlow<List<Detection>> = _detections.asStateFlow()

    private val _violations = MutableStateFlow<List<ViolationEvent>>(emptyList())
    val violations: StateFlow<List<ViolationEvent>> = _violations.asStateFlow()

    // ✅ lane 좌표 StateFlow
    private val _laneCoords = MutableStateFlow<List<List<Pair<Float, Float>>>>(emptyList())
    val laneCoords: StateFlow<List<List<Pair<Float, Float>>>> = _laneCoords.asStateFlow()

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
    private val activeModelKeys = listOf("final") // YOLO 모델 키
    private val cadence = mapOf("final" to 1)

    private var frameIndex = 0L

    val personCount = MutableStateFlow(0)

    var debugLastInferMs = MutableStateFlow(0L)
    var debugLastDetCount = MutableStateFlow(0)


    init {
        // ✅ Lane flow 구독 (LaneDetector는 Detector 내부에서 비동기로 계속 실행됨)
        if (detector is MultiModelInterpreterDetector) {
            viewModelScope.launch {
                detector.laneFlow
                    .filterNotNull()
                    .collect { laneResult ->
                        _laneCoords.value = laneResult.coords
                        Log.d("Lane_Debug", "laneFlow update coords=${laneResult.coords.size}")
                        Log.d("Lane_Debug", "laneFlow update: lanes=${laneResult.coords.size}, first=${laneResult.coords.firstOrNull()}")

                    }
            }
        }

        // ✅ 주기적으로 violation 갱신
        viewModelScope.launch {
            detectionUseCase.isDetectionActive.collect { isActive ->
                if (isActive) {
                    initializeCamera()
                }
                sessionHolder.startInstant
                    .filterNotNull()
                    .distinctUntilChanged()
                    .collect { startedAt ->
                        while (isActive) {
                            val now = Instant.now()
                            getViolationsInRangeUseCase(startedAt.toString(), now.toString())
                                .onSuccess { data ->
                                    _uiState.update { it.copy(violationDetections = data) }
                                }
                                .onFailure { e ->
                                    Log.e("Poll", "fetch fail: ${e.message}", e)
                                }
                            delay(5_000L)
                        }
                    }
            }
        }
    }
    fun initializeCamera() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isCameraReady = true,
                cameraPermissionGranted = true
            )
        }
    }

    fun navigateViolationDetail(violationId: String?) {
        viewModelScope.launch {
            navigator.navigate(Route.ViolationDetail(violationId))
        }
    }

    fun toggleFullscreen() {
        _uiState.value = _uiState.value.copy(
            isFullscreen = !_uiState.value.isFullscreen
        )
    }

    fun onViolationFilterSelected(filter: ViolationType) {
        _uiState.value = _uiState.value.copy(selectedViolationFilter = filter)
    }

    fun onViolationClick(violation: ViolationInRangeEntity) {
        viewModelScope.launch { navigator.navigate(Route.ViolationDetail(violation.id)) }
    }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }

    private fun filterViolations(violations: List<ViolationInRangeEntity>, filter: ViolationType): List<ViolationInRangeEntity> {
        return if (filter == ViolationType.ALL) violations else violations.filter { it.violationType.toString() == filter.toString() }
    }

    override fun onCleared() {
        detector.close()
        super.onCleared()
        camera = null
    }
}
