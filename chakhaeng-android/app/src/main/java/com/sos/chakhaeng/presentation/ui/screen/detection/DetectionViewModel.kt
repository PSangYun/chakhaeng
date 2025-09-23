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
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.usecase.violation.DetectionUseCase
import com.sos.chakhaeng.domain.usecase.violation.GetViolationsInRangeUseCase
import com.sos.chakhaeng.presentation.model.ViolationDetectionUiModel
import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import com.sos.chakhaeng.domain.usecase.ai.ProcessDetectionsUseCase
import com.sos.chakhaeng.presentation.mapper.ViolationUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val navigator: Navigator,
    private val detectionUseCase: DetectionUseCase,
    private val getViolationsInRangeUseCase: GetViolationsInRangeUseCase,
    private val detector: Detector,
    private val processDetectionsUseCase: ProcessDetectionsUseCase
) : ViewModel() {

    private val _detections = MutableStateFlow<List<Detection>>(emptyList())
    val detections = _detections.asStateFlow()

    private val _violations = MutableStateFlow<List<ViolationEvent>>(emptyList())
    val violations = _violations.asStateFlow()

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

    private val cadence = mapOf(
        "yolov8s" to 1
    )

    private var frameIndex = 0L

    @Volatile private var inFlightStartMs = 0L
    @Volatile private var gateHeld = false
    private val gateWatchdogMs = 2500L // 2.5초 넘게 잡혀있으면 비정상으로 간주

    val personCount = MutableStateFlow(0)
    private var hadPerson = false

    private fun isPerson(d: Detection): Boolean {
        // 라벨 파일이 COCO면 보통 "person" 이거나 인덱스 0
        return d.label.equals("person", ignoreCase = true) || d.label == "0"
    }

    init {
        viewModelScope.launch {
            detectionUseCase.isDetectionActive.collect { isActive ->
                if (isActive) {
                    initializeCamera()
                    generateSampleViolationData()
                    getViolationsInRangeUseCase("2025-09-12T05:59:21.093Z", "2025-09-22T05:59:21.093Z")
                } else {
                    pauseCamera()
                }
            }
        }
    }

    var debugLastInferMs = MutableStateFlow(0L)
    var debugLastParsedMs = MutableStateFlow(0L)
    var debugLastDetCount = MutableStateFlow(0)
    var debugFrames = MutableStateFlow(0L)
    var debugSkipped = MutableStateFlow(0L)

    fun onFrame(bitmap: Bitmap, rotation: Int): Boolean {
        if (!detectorReady.value) return false

        val now = System.currentTimeMillis()

        // 🔎 워치독: 게이트가 오래 잡혀 있으면 강제 해제
        if (gateHeld && now - inFlightStartMs > gateWatchdogMs) {
            Log.e("TAG", "watchdog: in-flight stuck for ${now - inFlightStartMs}ms -> force release")
            gateHeld = false
            try { inferGate.release() } catch (_: Throwable) {}
        }

        if (now - lastInferTs < minGapMs) return false

        // 게이트는 onFrame 안에서 즉시 획득/판단
        if (!inferGate.tryAcquire()) {
            debugSkipped.value = debugSkipped.value + 1
            if (debugSkipped.value % 30L == 0L) {
                Log.d("TAG", "skip frame: busy (in-flight), skipped=${debugSkipped.value}")
            }
            return false
        }

        // 게이트 정상 획득됨
        gateHeld = true
        inFlightStartMs = now
        lastInferTs = now

        val thisFrame = ++frameIndex

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val merged = ArrayList<Detection>(64)

                for (key in activeModelKeys) {
                    val period = cadence[key] ?: 1
                    if (thisFrame % period != 0L) continue

                    if (detector is MultiModelInterpreterDetector) detector.switchModel(key)

                    Log.d("DTAG", "call detect() now frame#$thisFrame key=$key")
                    val t0 = android.os.SystemClock.elapsedRealtime()

                    // ⏱ 전체 추론도 타임아웃으로 감싼다 (혹시 detect 내부가 블록되면)
                    val dets = runCatching {
                        withTimeout(3000) { detector.detect(bitmap, rotation) }
                    }.getOrElse { e ->
                        Log.e("TAG", "detect($key) failed: ${e.message}", e)
                        emptyList()
                    }

                    val t1 = android.os.SystemClock.elapsedRealtime()
                    debugLastInferMs.value = (t1 - t0)
                    debugLastDetCount.value = dets.size
                    Log.d("TAG", "infer key=$key took=${t1 - t0}ms, dets=${dets.size}")
                    merged += dets
                }

                val persons = merged.filter(::isPerson)
                personCount.value = persons.size

                if (persons.isNotEmpty()) {
                    Log.d("DET", "👤 persons=${persons.size}  scores=" +
                            persons.joinToString { "%.2f".format(it.score) })
                    val byLabel = merged.groupBy { it.label }
                    val summary = byLabel.entries
                        .sortedByDescending { it.value.size }
                        .joinToString(", ") { (label, list) -> "$label x${list.size}" }

                    Log.d("DET", "frame#$thisFrame classes: $summary")
                }

                if (!hadPerson && persons.isNotEmpty()) {
                    hadPerson = true
                    // ex) 간단 토스트/진동
                    // vibrator.vibrate(VibrationEffect.createOneShot(30, DEFAULT_AMPLITUDE))
                } else if (hadPerson && persons.isEmpty()) {
                    hadPerson = false
                }

                withContext(Dispatchers.Main) {
                    _detections.value = merged
                    _violations.value = processDetectionsUseCase(merged)
                }
            } finally {
                // ✅ 수명은 여기서 정리
                if (!bitmap.isRecycled) bitmap.recycle()
                gateHeld = false
                inFlightStartMs = 0L
                inferGate.release()
            }
        }

        return true
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
        viewModelScope.launch {
            navigator.navigate(Route.ViolationDetail(violation.id))
        }
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
                id = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                type = ViolationType.WRONG_WAY,
                licenseNumber = "12가1234",
                location = "강남구 테헤란로 123",
                detectedAt = LocalDateTime.now().minusMinutes(2),
                confidence = 0.95f,
                thumbnailUrl = null
            ),
            ViolationUiMapper.mapToUiModel(
                id ="3fa85f64-5717-4562-b3fc-2c963f66afa6",
                type = ViolationType.SIGNAL,
                licenseNumber = "34나5678",
                location = "서초구 서초대로 456",
                detectedAt = LocalDateTime.now().minusMinutes(5),
                confidence = 0.88f,
                thumbnailUrl = null
            ),
            ViolationUiMapper.mapToUiModel(
                id ="3fa85f64-5717-4562-b3fc-2c963f66afa6",
                type = ViolationType.LANE,
                licenseNumber = "56다9012",
                location = "마포구 월드컵로 789",
                detectedAt = LocalDateTime.now().minusMinutes(12),
                confidence = 0.73f,
                thumbnailUrl = null
            ),
            ViolationUiMapper.mapToUiModel(
                id = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
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