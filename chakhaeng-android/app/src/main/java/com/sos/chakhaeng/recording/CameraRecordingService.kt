package com.sos.chakhaeng.recording

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.ai.Detector
import com.sos.chakhaeng.core.camera.YuvToRgbConverter
import com.sos.chakhaeng.core.utils.DetectionSessionHolder
import com.sos.chakhaeng.core.camera.toBitmap
import com.sos.chakhaeng.domain.usecase.ai.ProcessDetectionsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale
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
    }

    private val SEGMENT_MS = 5_000L
    private val SEGMENT_COOLDOWN_MS = 150L
    private val PRE_WINDOW_LIMIT_MS = 12_000L
    private val DEFAULT_PRE_MS = 6_000L
    private val DEFAULT_POST_MS = 5_000L

    /** CameraX & Recording state */
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var imageAnalysis: ImageAnalysis? = null // TODO: 안정화 후 붙이기

    private var currentRecording: Recording? = null
    private var bufferingJob: Job? = null

    /** Analyzer(추후 사용) */
    private lateinit var analysisExecutor: ExecutorService

    private val _detections = MutableStateFlow<List<Detection>>(emptyList())
    fun detectionsFlow(): StateFlow<List<Detection>> = _detections

    private val serviceScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + Dispatchers.Default
    )

    private val detectorReady = MutableStateFlow(false)

    private val inferGate = kotlinx.coroutines.sync.Semaphore(1)
    private var lastInferTs = 0L
    private val minGapMs = 100L
    private lateinit var yuv: YuvToRgbConverter

    /** 링버퍼 */
    private data class Segment(val file: File, val startMs: Long, var endMs: Long)
    private val segmentQueue: ArrayDeque<Segment> = ArrayDeque()
    private val segMutex = Mutex()

    private data class CaptureRequest(
        val eventTs: Long,
        val preMs: Long,
        val postMs: Long,
        val displayName: String,
    )
    @Volatile private var pendingCapture: CaptureRequest? = null
    private var mergingJob: Job? = null

    /** Preview handling */
    private class HeadlessSurfaceProvider : Preview.SurfaceProvider {
        private var surfaceTexture: SurfaceTexture? = null
        private var surface: Surface? = null
        override fun onSurfaceRequested(request: SurfaceRequest) {
            Log.d(TAG, "HSP.onSurfaceRequested res=${request.resolution} thread=${Thread.currentThread().name}")
            val tex = SurfaceTexture(0).apply {
                setDefaultBufferSize(request.resolution.width, request.resolution.height)
            }
            val surf = Surface(tex)
            surfaceTexture = tex
            surface = surf
            Log.d(TAG, "HSP.provideSurface surface=$surf")
            request.provideSurface(surf, Runnable::run) {
                Log.d(TAG, "HSP.releaseCallback called -> releasing surface")
                runCatching { surf.release() }
                runCatching { tex.release() }
            }
        }
    }

    private var lastPreviewViewRef: WeakReference<PreviewView>? = null
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
            // UI 붙었을 때는 UI SurfaceProvider로 "재바인딩"
            lifecycleScope.launch { ensureCamera(view.surfaceProvider) }
        }
        fun detachPreview() {
            // 화면 떠날 땐 Headless로 "재바인딩"
            lifecycleScope.launch { ensureCamera(HeadlessSurfaceProvider()) }
        }
        fun startDetection() { startBuffering() }
        fun stopDetection()  { stopBuffering() }
        fun markIncident(preMs: Long, postMs: Long) { lifecycleScope.launch { markEvent(preMs, postMs) } }
    }
    private val binder = LocalBinder()
    override fun onBind(intent: Intent): IBinder { super.onBind(intent); return binder }

    /** Lifecycle */
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        analysisExecutor = Executors.newSingleThreadExecutor()
        Log.d(TAG, "onCreate() pid=${android.os.Process.myPid()} tid=${Thread.currentThread().name}")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: no CAMERA permission")
            stopSelf(); return
        }

        createNotificationChannel()
        yuv = YuvToRgbConverter(this)
        lifecycleScope.launch(Dispatchers.Default) {
            runCatching { detector.warmup() }
                .onSuccess {
                    detectorReady.value = true
                    Log.d("AI", "warmup ok")
                }
                .onFailure { Log.e("AI", "warmup fail", it) }
        }

        val notif = buildNotification("준비 중…")
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTI_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
        } else {
            startForeground(NOTI_ID, notif)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand action=${intent?.action}")
        when (intent?.action) {
            ACTION_START -> lifecycleScope.launch {
                // UI가 아직 없어도 Headless로 먼저 바인딩 보장
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
                val pre = intent.getLongExtra(EXTRA_PRE_MS, DEFAULT_PRE_MS)
                val post = intent.getLongExtra(EXTRA_POST_MS, DEFAULT_POST_MS)
                lifecycleScope.launch { markEvent(pre, post) }
            }
        }
        return START_STICKY
    }

    /** ✅ 한 곳에서만 바인딩: UI/Headless 공급자만 바꿔서 매번 "재바인딩" */
    private suspend fun ensureCamera(surfaceProvider: Preview.SurfaceProvider) = withContext(Dispatchers.Main) {
        val provider = cameraProvider ?: ProcessCameraProvider.getInstance(this@CameraRecordingService).get().also {
            cameraProvider = it
        }

        val pv = lastPreviewViewRef?.get()
        val rotation = pv?.display?.rotation ?: Surface.ROTATION_0

        // VideoCapture: 가볍게 (SD 우선)
        val qualitySelector = QualitySelector.fromOrderedList(
            listOf(Quality.SD, Quality.HD),
            FallbackStrategy.lowerQualityThan(Quality.SD)
        )
        val recorder = Recorder.Builder().setQualitySelector(qualitySelector).build()
        videoCapture = VideoCapture.withOutput(recorder).apply { targetRotation = rotation }

        // Preview
        preview = Preview.Builder()
            .setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_16_9)
            .setTargetRotation(rotation)
            .build().also { it.setSurfaceProvider(surfaceProvider) }

         imageAnalysis = ImageAnalysis.Builder()
             .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
             .setImageQueueDepth(1)
             .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
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
        Log.d(TAG, "ensureCamera(): bound (rotation=$rotation, provider=$surfaceProvider)")
        updateNotification("준비 완료")
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeFrame(image: ImageProxy) {
        try {
            if (!detectorReady.value) { image.close(); return }

            val now = android.os.SystemClock.elapsedRealtime()
            if (now - lastInferTs < minGapMs) { image.close(); return }
            if (!inferGate.tryAcquire()) { image.close(); return }
            lastInferTs = now

            // YUV -> Bitmap
            val bmp0 = image.toBitmap(yuvConverter)
            val rotation = image.imageInfo.rotationDegrees
            val bmp = if (rotation != 0) bmp0.rotateDeg(rotation) else bmp0
            image.close() // 비트맵으로 변환했으니 즉시 반납

            lifecycleScope.launch(Dispatchers.Default) {
                try {
                    val dets = withTimeoutOrNull(3000) { detector.detect(bmp, /*unused*/0) } ?: emptyList()

                    // 🔎 탐지 개수/상위 샘플 로깅
                    if (dets.isEmpty()) {
                        Log.d("AI", "no detections")
                    } else {
                        val top = dets.take(3).joinToString { "${it.label}@${"%.2f".format(it.score)}" }
                        Log.d("AI", "detections=${dets.size}, top=$top")
                    }

                    // UI에 흘리는 상태도 갱신
                    val prev = _detections.value
                    if (!sameDetections(prev, dets)) _detections.value = dets

                    val violations = processDetectionsUseCase(dets)
                    if (violations.isNotEmpty()) {
                        Log.d("AI", "violations=${violations.size} -> markEvent()")
                        markEvent(preMs = DEFAULT_PRE_MS, postMs = DEFAULT_POST_MS)
                    }
                } catch (t: Throwable) {
                    Log.e("AI", "detect failed: ${t.message}", t)
                } finally {
                    if (!bmp.isRecycled) bmp.recycle()
                    inferGate.release()
                }
            }
        } catch (t: Throwable) {
            Log.e("AI", "analyzeFrame error: ${t.message}", t)
            try { image.close() } catch (_: Throwable) {}
        }
    }

    // 추가: Bitmap 회전 헬퍼
    private fun Bitmap.rotateDeg(deg: Int): Bitmap {
        if (deg % 360 == 0) return this
        val m = android.graphics.Matrix().apply { postRotate(deg.toFloat()) }
        val rotated = Bitmap.createBitmap(this, 0, 0, width, height, m, true)
        if (rotated !== this) this.recycle()
        return rotated
    }


    private fun sameDetections(a: List<Detection>, b: List<Detection>): Boolean {
        if (a.size != b.size) return false
        for (i in a.indices) {
            val x = a[i]; val y = b[i]
            if (x.label != y.label) return false
            if (kotlin.math.abs(x.box.left - y.box.left) > 1e-4) return false
            if (kotlin.math.abs(x.box.top - y.box.top) > 1e-4) return false
            if (kotlin.math.abs(x.box.right - y.box.right) > 1e-4) return false
            if (kotlin.math.abs(x.box.bottom - y.box.bottom) > 1e-4) return false
        }
        return true
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

    /** 녹화 세그먼트 */
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
            Log.w("Recorder", "Start not received, skip segment (no valid data).")
            return@withContext null
        }

        delay(durationMs)
        rec.stop()
        val fin = finalizeDef.await()
        currentRecording = null

        if (fin.hasError()) {
            Log.e("Recorder", "segment finalize error: ${fin.error}")
            file.delete()
            return@withContext null
        }
        val endTs = System.currentTimeMillis()
        Segment(file = file, startMs = startTs, endMs = endTs)
    }

    private fun pruneOldSegmentsLocked(keepMs: Long) {
        val cutoff = System.currentTimeMillis() - keepMs
        while (segmentQueue.isNotEmpty() && segmentQueue.first().endMs < cutoff) {
            val removed = segmentQueue.removeFirst()
            removed.file.delete()
        }
    }

    /** Incident handling & merge */
    private fun markEvent(preMs: Long, postMs: Long) {
        val now = System.currentTimeMillis()
        val name = "incident_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))}.mp4"
        pendingCapture = CaptureRequest(eventTs = now, preMs = preMs, postMs = postMs, displayName = name)
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
    }

    private fun createNotificationChannel() {
        val ch = NotificationChannel(
            CHANNEL_ID,
            "Camera Recording",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Foreground camera recording"
            enableLights(false); enableVibration(false)
            lightColor = Color.BLUE
            setSound(null, null)
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
    }

    @SuppressLint("LaunchActivityFromNotification")
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
