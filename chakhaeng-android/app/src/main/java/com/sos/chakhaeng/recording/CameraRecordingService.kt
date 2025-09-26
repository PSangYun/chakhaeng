package com.sos.chakhaeng.recording

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.display.DisplayManager
import android.media.*
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import android.view.Display
import android.view.OrientationEventListener
import android.view.Surface
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.datatransport.runtime.scheduling.persistence.EventStoreModule_PackageNameFactory.packageName
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import com.sos.chakhaeng.core.ai.*
import com.sos.chakhaeng.ChakHaengApplication
import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.ai.Detector
import com.sos.chakhaeng.core.camera.YuvToRgbConverter
import com.sos.chakhaeng.core.camera.toBitmap
import com.sos.chakhaeng.core.utils.DetectionSessionHolder
import com.sos.chakhaeng.core.worker.getCurrentLocationAndEnqueue
import com.sos.chakhaeng.domain.usecase.ai.ProcessDetectionsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.max

private const val TAG = "CamSvc"

@AndroidEntryPoint
class CameraRecordingService : LifecycleService() {

    @Inject lateinit var detector: Detector
    @Inject lateinit var yuvConverter: YuvToRgbConverter
    @Inject lateinit var processDetectionsUseCase: ProcessDetectionsUseCase
    @Inject lateinit var detectionSessionHolder: DetectionSessionHolder

    companion object {
        const val CHANNEL_ID = "record_cam_channel"
        const val NOTI_ID = 7771

        const val ACTION_START = "com.sos.chakhaeng.action.START_RECORDING"
        const val ACTION_STOP = "com.sos.chakhaeng.action.STOP_RECORDING"
        const val ACTION_OPEN = "com.sos.chakhaeng.action.OPEN_APP"
        const val ACTION_MARK_EVENT = "com.sos.chakhaeng.action.MARK_EVENT"

        const val EXTRA_PRE_MS = "preMs"
        const val EXTRA_POST_MS = "postMs"
    }

    private val SEGMENT_MS = 5_000L
    private val SEGMENT_COOLDOWN_MS = 150L
    private val PRE_WINDOW_LIMIT_MS = 12_000L
    private val DEFAULT_PRE_MS = 3_000L
    private val DEFAULT_POST_MS = 5_000L

    /** CameraX & Recording state */
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var displayListener: DisplayManager.DisplayListener? = null
    private var currentRecording: Recording? = null
    private var bufferingJob: Job? = null
    private lateinit var analysisExecutor: ExecutorService

    private val _detections = MutableStateFlow<List<Detection>>(emptyList())
    fun detectionsFlow(): StateFlow<List<Detection>> = _detections

    private val _tracks = MutableStateFlow<List<TrackObj>>(emptyList())
    fun tracksFlow(): StateFlow<List<TrackObj>> = _tracks

    private val serviceScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + Dispatchers.Default
    )
    private val _lanes = MutableStateFlow(LaneDetection(emptyList()))
    fun lanesFlow(): StateFlow<LaneDetection> = _lanes

    private var orientationListener: OrientationEventListener? = null
    private var lastSurfaceRotation: Int = Surface.ROTATION_0

    private fun surfaceRotationFromDegrees(deg: Int): Int {
        if (deg == OrientationEventListener.ORIENTATION_UNKNOWN) return lastSurfaceRotation
        // 0/90/180/270으로 양자화 (45° 히스테리시스)
        return when (((deg + 45) / 90) % 4) {
            0 -> Surface.ROTATION_0
            1 -> Surface.ROTATION_90
            2 -> Surface.ROTATION_180
            else -> Surface.ROTATION_270
        }
    }

    private val detectorReady = MutableStateFlow(false)

    private val inferGate = kotlinx.coroutines.sync.Semaphore(1)
    private var lastInferTs = 0L
    private val minGapMs = 16L

    // 탐지(inference) FPS
    private var infFpsCnt = 0
    private var infFpsLast = SystemClock.elapsedRealtime()

    // 입력(analysis callback) FPS
    private var inFpsCnt = 0
    private var inFpsLast = SystemClock.elapsedRealtime()

    /** 링버퍼 */
    private data class Segment(val file: File, val startMs: Long, var endMs: Long)
    private val segmentQueue: ArrayDeque<Segment> = ArrayDeque()
    private val segMutex = Mutex()

    private data class CaptureRequest(
        val eventTs: Long,
        val preMs: Long,
        val postMs: Long,
        val displayName: String,
        val violationType : String = "신호위반",
        val plate : String = "무번호판"
    )
    @Volatile private var pendingCapture: CaptureRequest? = null
    private var mergingJob: Job? = null

    /** Preview handling */
    private class HeadlessSurfaceProvider : Preview.SurfaceProvider {
        private var surfaceTexture: SurfaceTexture? = null
        private var surface: Surface? = null
        override fun onSurfaceRequested(request: SurfaceRequest) {
            val tex = SurfaceTexture(0).apply { setDefaultBufferSize(request.resolution.width, request.resolution.height) }
            val surf = Surface(tex)
            surfaceTexture = tex
            surface = surf
            Log.d(TAG, "HSP.provideSurface surface=$surf")
            request.provideSurface(surf, Runnable::run) {
                runCatching { surf.release() }
                runCatching { tex.release() }
            }
        }
    }

    private var lastPreviewViewRef: WeakReference<PreviewView>? = null

    private fun currentRotation(): Int {
        // 1순위: OrientationEventListener가 마지막으로 기록한 값
        val cached = lastSurfaceRotation
        if (cached in arrayOf(
                Surface.ROTATION_0, Surface.ROTATION_90,
                Surface.ROTATION_180, Surface.ROTATION_270
            )
        ) return cached

        // 2순위: PreviewView.display
        val pv = lastPreviewViewRef?.get()
        pv?.display?.rotation?.let { return it }

        // 3순위: DisplayManager (기기에 따라 null/고정 0일 수 있음)
        val dm = getSystemService(DISPLAY_SERVICE) as DisplayManager
        val d = dm.getDisplay(Display.DEFAULT_DISPLAY)
        return d?.rotation ?: Surface.ROTATION_0
    }
    @Volatile private var cameraBound = false

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block()
        else Handler(Looper.getMainLooper()).post(block)
    }

    /** Binder */
    inner class LocalBinder : Binder() {
        fun getService(): CameraRecordingService = this@CameraRecordingService
        fun attachPreview(view: PreviewView) {
            lastPreviewViewRef = WeakReference(view)
            runOnMain {
                view.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                view.scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            lifecycleScope.launch { ensureCamera(view.surfaceProvider) }
        }
        fun detachPreview() {
            lifecycleScope.launch { ensureCamera(HeadlessSurfaceProvider()) }
        }
        fun startDetection() { startBuffering() }
        fun stopDetection()  { stopBuffering() }
        fun markIncident(preMs: Long, postMs: Long) { lifecycleScope.launch { markEvent(preMs, postMs) } }
    }
    private val binder = LocalBinder()
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    // ByteTrack
    private val byteTrack = ByteTrackEngine(
        scoreThresh = 0.1f,  // ↓ 약간 낮춰 끊김 구간의 약한 박스도 받기
        nmsThresh   = 0.70f,  // ↓ 중복 박스 정리 강화(유사 IoU 박스는 하나로)
        trackThresh = 0.10f,  // ↓ 약한 박스도 트랙 유지/연결 허용
        trackBuffer = 120,    // ↑ 일시 미검출을 더 오래 기억(30fps 기준 ~4초)
        matchThresh = 0.3f   // ↓ 재매칭 허들 낮춤(이게 핵심)
    )

    // 라벨 → 인덱스
    private val labelToIndex by lazy { TrafficLabels.LABELS.withIndex().associate { it.value.trim().lowercase() to it.index } }

    /** 디텍션 요약 로그 */
    private fun logDetStats(tag: String, dets: List<Detection>, w: Int, h: Int) {
        if (dets.isEmpty()) { Log.d(tag, "no dets"); return }
        val l = dets.take(5).joinToString { d ->
            val L = d.box.left; val T = d.box.top; val R = d.box.right; val B = d.box.bottom
            val W = R - L; val H = B - T
            val rawMax = maxOf(L, T, R, B)
            val unit = if (rawMax <= 1f) "N" else "PX"
            "(${d.label}@${"%.2f".format(d.score)} $unit) LTRB=[${"%.3f".format(L)},${"%.3f".format(T)},${"%.3f".format(R)},${"%.3f".format(B)}] WH=[${"%.3f".format(W)},${"%.3f".format(H)}]"
        }
        val zero = dets.count { (it.box.right - it.box.left) <= 0f || (it.box.bottom - it.box.top) <= 0f }
        val normLike = dets.count { maxOf(it.box.left, it.box.top, it.box.right, it.box.bottom) <= 1f }
        Log.d(tag, "frame=${w}x${h} dets=${dets.size} normGuess=$normLike zeroWH=$zero :: $l")
    }

    private fun iou(a: RectF, b: RectF): Float {
        val x1 = max(a.left, b.left)
        val y1 = max(a.top, b.top)
        val x2 = kotlin.math.min(a.right, b.right)
        val y2 = kotlin.math.min(a.bottom, b.bottom)
        val inter = kotlin.math.max(0f, x2 - x1) * kotlin.math.max(0f, y2 - y1)
        val ua = (a.right - a.left) * (a.bottom - a.top)
        val ub = (b.right - b.left) * (b.bottom - b.top)
        return inter / (ua + ub - inter + 1e-6f)
    }

    private fun nmsClassAgnostic(dets: List<Detection>, iouTh: Float = 0.55f): List<Detection> {
        val sorted = dets.sortedByDescending { it.score }.toMutableList()
        val out = mutableListOf<Detection>()
        while (sorted.isNotEmpty()) {
            val a = sorted.removeAt(0)
            out += a
            val it = sorted.iterator()
            while (it.hasNext()) {
                val b = it.next()
                if (iou(a.box, b.box) > iouTh) it.remove() // ← 라벨 비교 없음
            }
        }
        return out
    }
    // Detection → 정규화 LTRB [0,1] 로 변환
    private fun Detection.toNormLTRB(frameW: Int, frameH: Int): FloatArray? {
        // 원본값
        var l = box.left
        var t = box.top
        var r = box.right
        var b = box.bottom

        // 1) 좌표계가 픽셀이면 정규화
        val maxv = maxOf(l, t, r, b)
        if (maxv > 1f) {
            l /= frameW; r /= frameW
            t /= frameH; b /= frameH
        }

        // 2) LTRB인지 XYWH(센터)인지 판별 후 LTRB로 통일
        var w = r - l
        var h = b - t
        if (w <= 0f || h <= 0f) {
            // XYWH(센터 기준)로 가정
            val cx = l; val cy = t; val ww = r; val hh = b
            l = cx - ww / 2f
            t = cy - hh / 2f
            r = cx + ww / 2f
            b = cy + hh / 2f
            w = r - l
            h = b - t
        }

        // 3) 클램프 & 너무 작은 박스 제거(프레임의 ~1%)
        l = l.coerceIn(0f, 1f)
        t = t.coerceIn(0f, 1f)
        r = r.coerceIn(0f, 1f)
        b = b.coerceIn(0f, 1f)

        w = (r - l).coerceAtLeast(1e-6f)
        h = (b - t).coerceAtLeast(1e-6f)

        if (w < 0.01f || h < 0.01f) return null
        return floatArrayOf(l, t, w, h)
    }

    /** Lifecycle */
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        analysisExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            stopSelf(); return
        }

        createNotificationChannel()
        yuvConverter = YuvToRgbConverter(this)

        lifecycleScope.launch(Dispatchers.Default) {
            runCatching { detector.warmup() }
                .onSuccess { detectorReady.value = true }
                .onFailure { Log.e("AI", "warmup fail", it) }
        }

        if (detector is MultiModelInterpreterDetector) {
            (detector as MultiModelInterpreterDetector).laneFlow
                .filterNotNull()
                .onEach { lane -> _lanes.value = lane }
                .launchIn(lifecycleScope)
        }

        val notif = buildNotification("준비 중…")
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTI_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
        } else {
            startForeground(NOTI_ID, notif)
        }

        val dm = getSystemService(DISPLAY_SERVICE) as DisplayManager
        displayListener = object : DisplayManager.DisplayListener {
            override fun onDisplayChanged(id: Int) {
                val rot = currentRotation()
                preview?.targetRotation = rot
                imageAnalysis?.targetRotation = rot
                videoCapture?.targetRotation = rot
                Log.d("CamSvc", "rotation updated -> $rot")
                // 재바인딩은 필요 없음. targetRotation만 갱신하는 게 가장 안전.
            }
            override fun onDisplayAdded(id: Int) {}
            override fun onDisplayRemoved(id: Int) {}
        }
        orientationListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(degrees: Int) {
                val rot = surfaceRotationFromDegrees(degrees)
                if (rot != lastSurfaceRotation) {
                    lastSurfaceRotation = rot
                    // ▶ 화면/분석/녹화 모두 갱신
                    preview?.targetRotation = rot
                    imageAnalysis?.targetRotation = rot
                    videoCapture?.targetRotation = rot
                    Log.d(TAG, "orientation updated -> $rot ($degrees°)")
                }
            }
        }
        orientationListener?.enable()
        dm.registerDisplayListener(displayListener, Handler(Looper.getMainLooper()))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> lifecycleScope.launch {
                if (!cameraBound) ensureCamera(HeadlessSurfaceProvider())
                detectionSessionHolder.start()
                startBuffering()
            }
            ACTION_STOP -> {
                stopBuffering()
                stopSelf()
                detectionSessionHolder.clear()
            }
            ACTION_OPEN -> {
                val launch = packageManager.getLaunchIntentForPackage(packageName)
                launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (launch != null) startActivity(launch)
            }
            ACTION_MARK_EVENT -> {
                val pre = intent.getLongExtra(EXTRA_PRE_MS, 6000)
                val post = intent.getLongExtra(EXTRA_POST_MS, 5000)
                lifecycleScope.launch { markEvent(pre, post) }
            }
        }
        return START_STICKY
    }

    // ------------------- CameraX setup -------------------
    @SuppressLint("UnsafeOptInUsageError")
    private suspend fun ensureCamera(surfaceProvider: Preview.SurfaceProvider) = withContext(Dispatchers.Main) {
        val provider = cameraProvider ?: ProcessCameraProvider.getInstance(this@CameraRecordingService).get().also {
            cameraProvider = it
        }

        val analysisResolution = Size(1600, 533)
        val analysisSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(ResolutionStrategy(analysisResolution, ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
            .build()

        val rotation = currentRotation()

        val qualitySelector = QualitySelector.fromOrderedList(
            listOf(Quality.SD, Quality.HD),
            FallbackStrategy.lowerQualityThan(Quality.SD)
        )
        val recorder = Recorder.Builder().setQualitySelector(qualitySelector).build()
        videoCapture = VideoCapture.withOutput(recorder).apply { targetRotation = rotation }

        preview = Preview.Builder().setTargetRotation(rotation).build().also { it.setSurfaceProvider(surfaceProvider) }

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setImageQueueDepth(1)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setResolutionSelector(analysisSelector)
            .setTargetRotation(rotation)
            .build().apply { setAnalyzer(analysisExecutor, ::analyzeFrame) }

        provider.unbindAll()
        provider.bindToLifecycle(
            this@CameraRecordingService,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview!!,
            videoCapture!!,
            imageAnalysis!!
        )
        cameraBound = true
        updateNotification("준비 완료")
    }

    private fun detectLaneViolationSimple(
        detection: Detection?,        // YOLO 차량 1대
        laneDetection: LaneDetection  // LaneDetector 결과 (왼쪽 차선 1개만 있음)
    ): Boolean {
        if (detection == null) return false
        if (laneDetection.coords.isEmpty()) return false

        val leftLane = laneDetection.coords[0]

        // 🚘 차량 중심
        val cx = (detection.box.left + detection.box.right) / 2f
        val cy = detection.box.bottom

        // 현재 y 기준 차선의 x 좌표
        val laneX = interpolateX(leftLane, cy)

        // 🚨 침범: 차량 중심이 차선보다 왼쪽에 있으면 true
        return cx < laneX
    }

    // y 위치에 맞는 차선 x 좌표 보간
    private fun interpolateX(lane: List<Pair<Float, Float>>, y: Float): Float {
        val sorted = lane.sortedBy { it.second }
        for (i in 0 until sorted.size - 1) {
            val (x1, y1) = sorted[i]
            val (x2, y2) = sorted[i + 1]
            if (y1 <= y && y <= y2) {
                val t = (y - y1) / (y2 - y1)
                return x1 + t * (x2 - x1)
            }
        }
        return sorted.last().first
    }

    // ------------------- Analysis -------------------
    @OptIn(ExperimentalGetImage::class)
    private fun analyzeFrame(image: ImageProxy) {
        var gateAcquired = false
        var launchedJob = false
        try {
            inFpsCnt++
            val nowIn = SystemClock.elapsedRealtime()
            if (nowIn - inFpsLast >= 1000) {
                Log.d("AI", "input fps=$inFpsCnt")
                inFpsCnt = 0
                inFpsLast = nowIn
            }

            if (!detectorReady.value) { image.close(); return }

            val now = SystemClock.elapsedRealtime()
            if (now - lastInferTs < minGapMs) { image.close(); return }
            if (!inferGate.tryAcquire()) { image.close(); return }
            gateAcquired = true
            lastInferTs = now

            val rotation = image.imageInfo.rotationDegrees
            val bmp0 = image.toBitmap(yuvConverter)
            val bmp = if (rotation != 0) bmp0.rotateDeg(rotation) else bmp0
            image.close() // ImageProxy 반납
            image.close()

            lifecycleScope.launch(Dispatchers.Default) {
                launchedJob = true
                try {
                    // 🟢 YOLO 추론
                    val dets = withTimeoutOrNull(3000) { detector.detect(bmp, 0) } ?: emptyList()

                    infFpsCnt++
                    val nowInf = SystemClock.elapsedRealtime()
                    if (nowInf - infFpsLast >= 1000) {
                        Log.d("AI", "inference fps=$infFpsCnt")
                        infFpsCnt = 0
                        infFpsLast = nowInf
                    }

                    // 1) 원시 디텍션 로그
                    logDetStats("DET.Raw", dets, bmp.width, bmp.height)

                    // 2) 상태 갱신
                    // detection 상태 업데이트
                    val prev = _detections.value
                    if (!sameDetections(prev, dets)) _detections.value = dets

                    // 3) 차량 필터
                    val vehIdxSet = TrafficLabels.VEH_IDX
                    val rawVeh = dets.filter { d ->
                        val key = d.label.trim().lowercase()
                        val idx = labelToIndex[key] ?: d.label.toIntOrNull()
                        idx?.let { it in vehIdxSet } == true
                    }
                    logDetStats("DET.Veh", rawVeh, bmp.width, bmp.height)
                    val vehDedup = nmsClassAgnostic(rawVeh)
                    // 4) 좌표 변환: 어떤 입력이 와도 정규화 LTRB로 통일
                    val vehForTrack = rawVeh.mapNotNull { d ->
                        val labIdx = labelToIndex[d.label.trim().lowercase()] ?: d.label.toIntOrNull() ?: return@mapNotNull null
                        val nb = d.toNormLTRB(bmp.width, bmp.height) ?: return@mapNotNull null
                        ByteTrackEngine.Det(category = labIdx, conf = d.score, x = nb[0], y = nb[1], w = nb[2], h = nb[3])
                    }.also { list ->
                        val brief = list.take(5).joinToString {
                            "c=${it.category}@${"%.2f".format(it.conf)} N=[${"%.3f".format(it.x)},${"%.3f".format(it.y)},${"%.3f".format(it.w)},${"%.3f".format(it.h)}]"
                        }
                        Log.d("BT.InN", "N dets=${list.size} :: $brief")
                    }

// 5) ByteTrack 업데이트 (이미 정규화이므로 normW/H 넘기지 않음)
                    val tracksRaw = byteTrack.update(dets = vehForTrack)
                    tracksRaw.take(5).forEach {
                        Log.d("BT.DebugOutN", "N x=${"%.3f".format(it.x)} y=${"%.3f".format(it.y)} w=${"%.3f".format(it.w)} h=${"%.3f".format(it.h)}")
                    }
                    // 6) 출력 검증/로그
                    val oobOut = tracksRaw.filter { t ->
                        t.x < 0f || t.y < 0f || t.w <= 0f || t.h <= 0f ||
                                (t.x + t.w) > 1f || (t.y + t.h) > 1f
                    }
                    if (oobOut.isNotEmpty()) {
                        Log.w("BT.Out", "out-of-range: " + oobOut.joinToString {
                            "ID=${it.id} N=[${"%.3f".format(it.x)},${"%.3f".format(it.y)},${"%.3f".format(it.w)},${"%.3f".format(it.h)}]"
                        })
                    } else {
                        val brief = tracksRaw.take(4).joinToString {
                            "ID=${it.id} c=${it.category} conf=${"%.2f".format(it.conf)}"
                        }
                        Log.d("BT.Out", "tracks=${tracksRaw.size} :: $brief")
                    }

                    // 7) UI 전달
                    val trackObjs = tracksRaw.map { it.toTrackObj() }
                    _tracks.value = trackObjs

                    val violations = processDetectionsUseCase(dets, trackObjs)
                    // violation 발생 시 이벤트 마킹
                    if (violations.isNotEmpty()) {
                        val chosen = violations.first()
                        val violationType = chosen.type
                        val plate = resolvePlate(dets) ?: "무번호판"  // 지금은 placeholder
                        markEvent(preMs = DEFAULT_PRE_MS, postMs = DEFAULT_POST_MS, violationType = violationType, plate = plate)
                    }

                    // 🟢 LaneDetector에 프레임 전달 (YOLO와 별개 스레드에서 동작)
                    if (detector is MultiModelInterpreterDetector) {
                        Log.d("Lane_Debug", "submitLaneFrame 호출됨: w=${bmp.width}, h=${bmp.height}")
                        (detector as MultiModelInterpreterDetector).submitLaneFrame(
                            bmp.copy(bmp.config ?: Bitmap.Config.ARGB_8888, false)
                        )
                    }

                    val lane = _lanes.value

// 🚘 차량 (car/truck/bus만 고려)
                    val car = dets.firstOrNull { it.label.lowercase() in listOf("car", "truck", "bus") }

// 🚨 침범 체크
                    if (detectLaneViolationSimple(car, lane)) {
                        Log.d("Violation", "중앙선 침범 발생!")
                    }

                } finally {
                    if (!bmp.isRecycled) bmp.recycle()
                    if (gateAcquired) inferGate.release()
                }
            }
        } catch (t: Throwable) {
            Log.e("AI", "analyzeFrame error: ${t.message}", t)
            try { image.close() } catch (_: Throwable) {}
            if (gateAcquired) inferGate.release()
        }
    }

    private fun Bitmap.rotateDeg(deg: Int): Bitmap {
        if (deg % 360 == 0) return this
        val m = Matrix().apply { postRotate(deg.toFloat()) }
        val rotated = Bitmap.createBitmap(this, 0, 0, width, height, m, true)
        if (rotated !== this) this.recycle()
        return rotated
    }

    private fun sameDetections(a: List<Detection>, b: List<Detection>): Boolean {
        if (a.size != b.size) return false
        return a.indices.all { i ->
            val x = a[i]; val y = b[i]
            x.label == y.label &&
                    kotlin.math.abs(x.box.left - y.box.left) < 1e-4 &&
                    kotlin.math.abs(x.box.top - y.box.top) < 1e-4 &&
                    kotlin.math.abs(x.box.right - y.box.right) < 1e-4 &&
                    kotlin.math.abs(x.box.bottom - y.box.bottom) < 1e-4
        }
    }

    // ------------------- Buffering & Merge -------------------
    // CameraRecordingService.kt
    private fun resolvePlate(dets: List<Detection>): String? {
        // TODO: 번호판 detector/OCR 붙이면 여기에서 실제 텍스트 반환
        //  - 예: dets에서 "plate" 라벨 찾아 OCR 결과 매핑
        return null // 지금은 없는 경우 null -> "무번호판"으로 대체
    }


    /** Ring buffer loop */
    private fun startBuffering() {
        if (bufferingJob?.isActive == true) return
        updateNotification("버퍼링 시작…")
        bufferingJob = lifecycleScope.launch(Dispatchers.Default) {
            while (isActive) {
                val seg = recordOneSegment(SEGMENT_MS)
                if (seg == null) { delay(120); continue }
                segMutex.withLock {
                    segmentQueue.addLast(seg)
                    pruneOldSegmentsLocked(keepMs = PRE_WINDOW_LIMIT_MS)
                }
                checkAndMaybeMerge()
                delay(SEGMENT_COOLDOWN_MS)
            }
        }
    }

    private fun stopBuffering() {
        bufferingJob?.cancel(); bufferingJob = null
        currentRecording?.stop(); currentRecording = null
        updateNotification("버퍼링 중지")
        lifecycleScope.launch(Dispatchers.IO) {
            segMutex.withLock {
                segmentQueue.forEach { it.file.delete() }
                segmentQueue.clear()
            }
        }
    }

    private suspend fun recordOneSegment(durationMs: Long): Segment? = withContext(Dispatchers.Main) {
        val vc = videoCapture ?: return@withContext null
        val startTs = System.currentTimeMillis()
        val file = File(externalCacheDir ?: cacheDir, "seg_${startTs}.mp4")
        val output = FileOutputOptions.Builder(file).build()

        val startDef = CompletableDeferred<Boolean>()
        val finalizeDef = CompletableDeferred<VideoRecordEvent.Finalize>()

        val rec = vc.output.prepareRecording(this@CameraRecordingService, output)
            .start(ContextCompat.getMainExecutor(this@CameraRecordingService)) { ev ->
                when (ev) {
                    is VideoRecordEvent.Start -> startDef.complete(true)
                    is VideoRecordEvent.Finalize -> {
                        if (!startDef.isCompleted) startDef.complete(false)
                        finalizeDef.complete(ev)
                    }
                    else -> Unit
                }
            }
        currentRecording = rec

        val started = withTimeoutOrNull(2_500) { startDef.await() } == true
        if (!started) {
            rec.stop()
            runCatching { finalizeDef.await() }
            currentRecording = null
            file.delete()
            return@withContext null
        }

        delay(durationMs)
        rec.stop()
        val fin = finalizeDef.await()
        currentRecording = null
        if (fin.hasError()) { file.delete(); return@withContext null }
        val endTs = System.currentTimeMillis()
        Segment(file, startTs, endTs)
    }

    private fun pruneOldSegmentsLocked(keepMs: Long) {
        val cutoff = System.currentTimeMillis() - keepMs
        while (segmentQueue.isNotEmpty() && segmentQueue.first().endMs < cutoff) {
            val removed = segmentQueue.removeFirst()
            removed.file.delete()
        }
    }

    private fun markEvent(preMs: Long, postMs: Long) {
        markEvent(
            preMs = preMs,
            postMs = postMs,
            violationType = "UNKNOWN",   // 기본값 (수동 트리거 등)
            plate = "무번호판"
        )
    }

    /** Incident handling & merge */
    private fun markEvent(preMs: Long, postMs: Long, violationType: String, plate: String) {
        val now = System.currentTimeMillis()
        val name = "incident_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))}.mp4"
        val textToRead = "$violationType 감지되었습니다."
        if (ChakHaengApplication.ttsReady) {
            ChakHaengApplication.tts.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "FCM_TTS")
        } else {
            Log.w(TAG, "TTS 준비 안 됨, 음성 출력 건너뜀")
        }
        // 넣어줘 상윤아
        pendingCapture = CaptureRequest(eventTs = now, preMs = preMs, postMs = postMs, displayName = name, violationType = violationType, plate = plate)
        updateNotification("사건 감지! 후단 ${postMs / 1000}s 수집 중…")
    }

    private fun createVideoUri(displayName: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ChakHaeng/Incidents")
            put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Failed to insert MediaStore row")
    }

    private suspend fun checkAndMaybeMerge() {
        val req = pendingCapture ?: return

        val enough = segMutex.withLock {
            segmentQueue.isNotEmpty() && (segmentQueue.last().endMs >= req.eventTs + req.postMs)
        }
        if (!enough) return

        val sources: List<File> = segMutex.withLock {
            val fromTs = req.eventTs - req.preMs
            val toTs = req.eventTs + req.postMs
            segmentQueue.filter { it.endMs >= fromTs }
                .takeWhile { it.startMs <= toTs }
                .map { it.file }
                .toList()
        }
        if (sources.isEmpty() || mergingJob?.isActive == true) return

        mergingJob = lifecycleScope.launch(Dispatchers.Default) {
            var outUri: Uri? = null
            try {
                updateNotification("사건 클립 병합 중 (${sources.size}개)…")

                outUri = createVideoUri(req.displayName)
                mergeMp4SegmentsToUri(sources, outUri)
                getCurrentLocationAndEnqueue(this@CameraRecordingService, outUri, req.violationType, req.plate )
                notifyIncidentSaved(outUri)
                updateNotification("사건 저장 완료: ${req.displayName}")
            } catch (t: Throwable) {
                Log.e("Recorder", "merge error: ${t.message}", t)
                updateNotification("사건 저장 실패")
                runCatching { outUri?.let { contentResolver.delete(it, null, null) } }
            } finally {
                pendingCapture = null
                segMutex.withLock { pruneOldSegmentsLocked(keepMs = PRE_WINDOW_LIMIT_MS) }
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun mergeMp4SegmentsToUri(inputs: List<File>, outUri: Uri): Uri {
        val pfd = contentResolver.openFileDescriptor(outUri, "w") ?: error("openFD failed")
        val muxer = MediaMuxer(pfd.fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var dstVideoTrack = -1
        var started = false
        var orientationHint = 0

        val buffer = ByteBuffer.allocate(2 * 1024 * 1024)
        val info = MediaCodec.BufferInfo()
        var ptsOffsetUs = 0L
        var maxPtsUs = 0L
        fun selectTrack(ex: MediaExtractor, prefix: String): Int {
            for (i in 0 until ex.trackCount) {
                val fmt = ex.getTrackFormat(i)
                val mime = fmt.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith(prefix)) return i
            }
            return -1
        }
        inputs.forEach { file ->
            val ex = MediaExtractor().apply { setDataSource(file.absolutePath) }
            val vIdx = selectTrack(ex, "video/")
            if (!started) {
                if (vIdx != -1) {
                    val srcFmt = ex.getTrackFormat(vIdx)
                    if (srcFmt.containsKey(MediaFormat.KEY_ROTATION)) {
                        orientationHint = srcFmt.getInteger(MediaFormat.KEY_ROTATION)
                    }
                    muxer.setOrientationHint(orientationHint)

                    dstVideoTrack = muxer.addTrack(srcFmt)
                }
                muxer.start(); started = true
            } else {
                if (vIdx != -1) {
                    val srcFmt = ex.getTrackFormat(vIdx)
                    val rot = if (srcFmt.containsKey(MediaFormat.KEY_ROTATION))
                        srcFmt.getInteger(MediaFormat.KEY_ROTATION) else 0
                    if (rot != orientationHint) {
                        Log.w("Mux", "segment rotation differs: $rot vs $orientationHint (index=)")
                    }
                }
            }
            if (vIdx != -1 && dstVideoTrack != -1) {
                ex.selectTrack(vIdx)
                while (true) {
                    val size = ex.readSampleData(buffer, 0)
                    if (size < 0) break
                    info.offset = 0
                    info.size = size
                    info.presentationTimeUs = ex.sampleTime + ptsOffsetUs
                    info.flags = ex.sampleFlags
                    muxer.writeSampleData(dstVideoTrack, buffer, info)
                    maxPtsUs = maxOf(maxPtsUs, info.presentationTimeUs)
                    ex.advance()
                }
                ex.unselectTrack(vIdx)
            }
            ex.release()
            ptsOffsetUs = maxPtsUs
        }
        muxer.stop(); muxer.release(); pfd.close()
        val cv = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
        contentResolver.update(outUri, cv, null, null)
        return outUri
    }

    /** Notifications */
    private fun notifyIncidentSaved(uri: Uri) {
        val open = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val openPending = PendingIntent.getActivity(
            this, 2001, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val noti = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setContentTitle("사건 영상 저장됨")
            .setContentText("탭하면 재생합니다")
            .addAction(android.R.drawable.ic_media_play, "보기", openPending)
            .setContentIntent(openPending)
            .setOngoing(false)
            .build()
        nm.notify(8891, noti)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBuffering()
        cameraProvider?.unbindAll()
        // 회전 리스너 해제
        (getSystemService(DISPLAY_SERVICE) as? DisplayManager)
            ?.unregisterDisplayListener(displayListener)
        displayListener = null

        // Analyzer 스레드 정리
        runCatching { analysisExecutor.shutdown() }
        orientationListener?.disable()
        orientationListener = null
    }

    private fun createNotificationChannel() {
        val ch = NotificationChannel(CHANNEL_ID, "Camera Recording", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Foreground camera recording"
            enableLights(false); enableVibration(false)
            lightColor = Color.BLUE
            setSound(null, null)
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
    }

    private fun buildNotification(content: String): Notification {
        val stopIntent = Intent(this, CameraRecordingService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val openIntent = Intent(this, CameraRecordingService::class.java).apply { action = ACTION_OPEN }
        val openPending = PendingIntent.getService(
            this, 1, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setContentTitle("ChakHaeng 녹화")
            .setContentText(content)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "중지", stopPending)
            .setContentIntent(openPending)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification(content: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTI_ID, buildNotification(content))
    }
}
