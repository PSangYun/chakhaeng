package com.sos.chakhaeng.presentation.ui.screen.detection

import android.graphics.Bitmap
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
import com.sos.chakhaeng.domain.usecase.violation.DetectionUseCase
import com.sos.chakhaeng.domain.usecase.violation.GetViolationsInRangeUseCase
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.model.violation.ViolationInRangeEntity
import com.sos.chakhaeng.domain.usecase.ai.ProcessDetectionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val navigator: Navigator,
    private val detectionUseCase: DetectionUseCase,
    private val getViolationsInRangeUseCase: GetViolationsInRangeUseCase,
    private val detector: Detector,
    private val processDetectionsUseCase: ProcessDetectionsUseCase,
    private val sessionHolder: DetectionSessionHolder
) : ViewModel() {

    private val _detections = MutableStateFlow<List<Detection>>(emptyList())
    val detections: StateFlow<List<Detection>> = _detections.asStateFlow()

    private val _violations = MutableStateFlow<List<ViolationEvent>>(emptyList())
    val violations: StateFlow<List<ViolationEvent>> = _violations.asStateFlow()

    // ✅ lane 좌표 StateFlow
    private val _laneCoords = MutableStateFlow<List<List<Pair<Float, Float>>>>(emptyList())
    val laneCoords: StateFlow<List<List<Pair<Float, Float>>>> = _laneCoords.asStateFlow()

    private val inferGate = Semaphore(1)
    private var lastInferTs = 0L
    private val minGapMs = 80L

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
                        Log.d("Lane_Final", "laneFlow update coords=${laneResult.coords.size}")
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

    fun onFrame(bitmap: Bitmap, rotation: Int): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastInferTs < minGapMs) return false
        if (!inferGate.tryAcquire()) return false

        lastInferTs = now
        val thisFrame = ++frameIndex

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val merged = ArrayList<Detection>(64)

                for (key in activeModelKeys) {
                    val period = cadence[key] ?: 1
                    if (thisFrame % period != 0L) continue
                    if (detector is MultiModelInterpreterDetector) detector.switchModel(key)

                    val t0 = android.os.SystemClock.elapsedRealtime()
                    val yoloDetections: List<Detection> = runCatching {
                        withTimeout(3000) { detector.detect(bitmap, rotation) }
                    }.getOrElse { e ->
                        Log.e("TAG", "detect($key) failed: ${e.message}", e)
                        emptyList()
                    }

                    merged += yoloDetections

                    val t1 = android.os.SystemClock.elapsedRealtime()
                    debugLastInferMs.value = (t1 - t0)
                    debugLastDetCount.value = yoloDetections.size
                }

                // ✅ LaneDetector는 Detector 내부 워커가 별도 수행 → 여기선 submit만
                if (detector is MultiModelInterpreterDetector) {
                    detector.submitLaneFrame(bitmap)
                }

                withContext(Dispatchers.Main) {
                    _detections.value = merged
                    _violations.value = processDetectionsUseCase(merged)
                }
            } finally {
                if (!bitmap.isRecycled) bitmap.recycle()
                inferGate.release()
            }
        }
        return true
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

    fun onCameraReady(camera: Camera) { this.camera = camera }
    fun pauseCamera() { _uiState.value = _uiState.value.copy(isCameraReady = false) }
    fun toggleFullscreen() { _uiState.value = _uiState.value.copy(isFullscreen = !_uiState.value.isFullscreen) }
    fun onViolationFilterSelected(filter: ViolationType) { _uiState.value = _uiState.value.copy(selectedViolationFilter = filter) }
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
