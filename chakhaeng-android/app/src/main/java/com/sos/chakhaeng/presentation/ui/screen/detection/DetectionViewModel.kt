package com.sos.chakhaeng.presentation.ui.screen.detection

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.Camera
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.ai.Detector
import com.sos.chakhaeng.core.ai.LaneDetection
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    val detections = _detections.asStateFlow()

    private val _violations = MutableStateFlow<List<ViolationEvent>>(emptyList())
    val violations = _violations.asStateFlow()

    // ✅ lane 좌표 StateFlow
    private val _laneCoords = MutableStateFlow<List<List<Pair<Float, Float>>>>(emptyList())
    val laneCoords: StateFlow<List<List<Pair<Float, Float>>>> = _laneCoords.asStateFlow()

    private val inferGate = Semaphore(1)
    private val detectorReady = MutableStateFlow(false)

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
    private val activeModelKeys = listOf("yolov8s")
    private val cadence = mapOf("yolov8s" to 1)

    private var frameIndex = 0L
    private val gateWatchdogMs = 2500L

    val personCount = MutableStateFlow(0)
    private var hadPerson = false

    private fun isPerson(d: Detection): Boolean {
        return d.label.equals("person", ignoreCase = true) || d.label == "0"
    }

    init {
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

    var debugLastInferMs = MutableStateFlow(0L)
    var debugLastDetCount = MutableStateFlow(0)

    fun onFrame(bitmap: Bitmap, rotation: Int): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastInferTs < minGapMs) return false
        if (!inferGate.tryAcquire()) return false

        lastInferTs = now
        val thisFrame = ++frameIndex

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val merged = ArrayList<Detection>(64)
                var latestLaneCoords: List<List<Pair<Float, Float>>> = emptyList()

                for (key in activeModelKeys) {
                    val period = cadence[key] ?: 1
                    if (thisFrame % period != 0L) continue
                    if (detector is MultiModelInterpreterDetector) detector.switchModel(key)

                    val t0 = android.os.SystemClock.elapsedRealtime()
                    val dets: Pair<List<Detection>, LaneDetection> = runCatching {
                        withTimeout(3000) { detector.detect(bitmap, rotation) }
                    }.getOrElse { e ->
                        Log.e("TAG", "detect($key) failed: ${e.message}", e)
                        Pair(emptyList(), LaneDetection(emptyList()))
                    }

                    val (yoloDetections, laneResult) = dets
                    merged += yoloDetections
                    latestLaneCoords = laneResult.coords

                    val t1 = android.os.SystemClock.elapsedRealtime()
                    debugLastInferMs.value = (t1 - t0)
                    debugLastDetCount.value = yoloDetections.size
                }

                withContext(Dispatchers.Main) {
                    _detections.value = merged
                    _violations.value = processDetectionsUseCase(merged)
                    _laneCoords.value = latestLaneCoords
                    Log.d("Lane_Final", "update lane coords=${latestLaneCoords.size}")
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
