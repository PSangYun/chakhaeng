package com.sos.chakhaeng.recording

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.*
import android.graphics.SurfaceTexture
import android.hardware.display.DisplayManager
import android.media.*
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.util.Size
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
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.sos.chakhaeng.core.ai.*
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

        const val DEFAULT_PRE_MS = 6000L
        const val DEFAULT_POST_MS = 5000L
    }

    /** CameraX & Recording state */
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var displayListener: DisplayManager.DisplayListener? = null
    private var currentRecording: Recording? = null
    private var bufferingJob: Job? = null
    private lateinit var analysisExecutor: ExecutorService

    /** Detection state */
    private val _detections = MutableStateFlow<List<Detection>>(emptyList())
    fun detectionsFlow(): StateFlow<List<Detection>> = _detections

    private val _lanes = MutableStateFlow(LaneDetection(emptyList()))
    fun lanesFlow(): StateFlow<LaneDetection> = _lanes

    private var orientationListener: OrientationEventListener? = null
    private var lastSurfaceRotation: Int = Surface.ROTATION_0
    private val detectorReady = MutableStateFlow(false)

    private val inferGate = kotlinx.coroutines.sync.Semaphore(1)
    private var lastInferTs = 0L
    private val minGapMs = 16L

    /** Ring buffer */
    private data class Segment(val file: File, val startMs: Long, var endMs: Long)
    private val segmentQueue: ArrayDeque<Segment> = ArrayDeque()
    private val segMutex = Mutex()

    private data class CaptureRequest(val eventTs: Long, val preMs: Long, val postMs: Long, val displayName: String)
    @Volatile private var pendingCapture: CaptureRequest? = null
    private var mergingJob: Job? = null

    /** Preview handling */
    private class HeadlessSurfaceProvider : Preview.SurfaceProvider {
        override fun onSurfaceRequested(request: SurfaceRequest) {
            val tex = SurfaceTexture(0).apply { setDefaultBufferSize(request.resolution.width, request.resolution.height) }
            val surf = Surface(tex)
            request.provideSurface(surf, Runnable::run) {
                runCatching { surf.release() }
                runCatching { tex.release() }
            }
        }
    }

    private var lastPreviewViewRef: WeakReference<PreviewView>? = null
    @Volatile private var cameraBound = false

    /** Binder for Compose */
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
    }
    private val binder = LocalBinder()
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block()
        else Handler(Looper.getMainLooper()).post(block)
    }

    // ------------------- Lifecycle -------------------
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

        val rotation = lastSurfaceRotation
        val qualitySelector = QualitySelector.fromOrderedList(listOf(Quality.SD, Quality.HD), FallbackStrategy.lowerQualityThan(Quality.SD))
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
        provider.bindToLifecycle(this@CameraRecordingService, CameraSelector.DEFAULT_BACK_CAMERA, preview!!, videoCapture!!, imageAnalysis!!)
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
        try {
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

            lifecycleScope.launch(Dispatchers.Default) {
                try {
                    // 🟢 YOLO 추론
                    val dets = withTimeoutOrNull(3000) { detector.detect(bmp, 0) } ?: emptyList()

                    // detection 상태 업데이트
                    val prev = _detections.value
                    if (!sameDetections(prev, dets)) _detections.value = dets

                    // violation 발생 시 이벤트 마킹
                    val violations = processDetectionsUseCase(dets)
                    if (violations.isNotEmpty()) {
                        markEvent(preMs = DEFAULT_PRE_MS, postMs = DEFAULT_POST_MS)
                    }

                    // 🟢 LaneDetector에 프레임 전달 (YOLO와 별개 스레드에서 동작)
                    if (detector is MultiModelInterpreterDetector) {
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
    private fun startBuffering() {
        if (bufferingJob?.isActive == true) return
        updateNotification("버퍼링 시작…")
        bufferingJob = lifecycleScope.launch(Dispatchers.Default) {
            while (isActive) {
                val seg = recordOneSegment(5000)
                if (seg == null) { delay(120); continue }
                segMutex.withLock {
                    segmentQueue.addLast(seg)
                    pruneOldSegmentsLocked(12000)
                }
                checkAndMaybeMerge()
                delay(150)
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
        val now = System.currentTimeMillis()
        val name = "incident_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))}.mp4"
        pendingCapture = CaptureRequest(now, preMs, postMs, name)
        updateNotification("사건 감지! 후단 ${postMs / 1000}s 수집 중…")
    }

    private suspend fun checkAndMaybeMerge() {
        val req = pendingCapture ?: return
        val enough = segMutex.withLock {
            segmentQueue.isNotEmpty() && (segmentQueue.last().endMs >= req.eventTs + req.postMs)
        }
        if (!enough) return

        val sources = segMutex.withLock {
            val fromTs = req.eventTs - req.preMs
            val toTs = req.eventTs + req.postMs
            segmentQueue.filter { it.endMs >= fromTs }.takeWhile { it.startMs <= toTs }.map { it.file }
        }
        if (sources.isEmpty() || mergingJob?.isActive == true) return

        mergingJob = lifecycleScope.launch(Dispatchers.Default) {
            var outUri: Uri? = null
            try {
                updateNotification("사건 클립 병합 중 (${sources.size}개)…")
                outUri = createVideoUri(req.displayName)
                mergeMp4SegmentsToUri(sources, outUri)
                getCurrentLocationAndEnqueue(this@CameraRecordingService, outUri, "신호위반", "12가1234")
                notifyIncidentSaved(outUri)
                updateNotification("사건 저장 완료: ${req.displayName}")
            } catch (t: Throwable) {
                Log.e("Recorder", "merge error: ${t.message}", t)
                updateNotification("사건 저장 실패")
                runCatching { outUri?.let { contentResolver.delete(it, null, null) } }
            } finally {
                pendingCapture = null
                segMutex.withLock { pruneOldSegmentsLocked(12000) }
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun mergeMp4SegmentsToUri(inputs: List<File>, outUri: Uri): Uri {
        val pfd = contentResolver.openFileDescriptor(outUri, "w") ?: error("openFD failed")
        val muxer = MediaMuxer(pfd.fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var dstVideoTrack = -1
        var started = false
        val buffer = ByteBuffer.allocate(2 * 1024 * 1024)
        val info = MediaCodec.BufferInfo()
        var ptsOffsetUs = 0L
        var maxPtsUs = 0L
        fun selectTrack(ex: MediaExtractor, prefix: String): Int {
            for (i in 0 until ex.trackCount) {
                val mime = ex.getTrackFormat(i).getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith(prefix)) return i
            }
            return -1
        }
        inputs.forEach { file ->
            val ex = MediaExtractor().apply { setDataSource(file.absolutePath) }
            val vIdx = selectTrack(ex, "video/")
            if (!started) {
                if (vIdx != -1) dstVideoTrack = muxer.addTrack(ex.getTrackFormat(vIdx))
                muxer.start(); started = true
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

    // ------------------- Notifications -------------------
    private fun createVideoUri(displayName: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ChakHaeng/Incidents")
            put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: error("insert fail")
    }

    private fun notifyIncidentSaved(uri: Uri) {
        val open = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val openPending = PendingIntent.getActivity(this, 2001, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
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
        val stopPending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val openIntent = Intent(this, CameraRecordingService::class.java).apply { action = ACTION_OPEN }
        val openPending = PendingIntent.getService(this, 1, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
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

    override fun onDestroy() {
        super.onDestroy()
        stopBuffering()
        cameraProvider?.unbindAll()

        val dm = getSystemService(DISPLAY_SERVICE) as? DisplayManager
        displayListener?.let {
            dm?.unregisterDisplayListener(it)
        }
        displayListener = null

        runCatching { analysisExecutor.shutdown() }
        orientationListener?.disable()
        orientationListener = null
    }
}
