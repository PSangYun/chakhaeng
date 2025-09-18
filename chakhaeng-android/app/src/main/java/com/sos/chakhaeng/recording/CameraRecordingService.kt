package com.sos.chakhaeng.recording

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaScannerConnection
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Surface
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.ExperimentalPersistentRecording
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import kotlin.math.max

class CameraRecordingService : LifecycleService() {

    companion object {
        const val CHANNEL_ID = "record_cam_channel"
        const val NOTI_ID = 7771
        const val ACTION_START = "com.sos.chakhaeng.action.START_RECORDING"
        const val ACTION_STOP  = "com.sos.chakhaeng.action.STOP_RECORDING"
        const val ACTION_OPEN  = "com.sos.chakhaeng.action.OPEN_APP"
        const val ACTION_MARK_EVENT = "com.sos.chakhaeng.action.MARK_EVENT" // << 추가

        const val EXTRA_PRE_MS = "preMs"
        const val EXTRA_POST_MS = "postMs"
    }

    private val SEGMENT_MS = 2_000L
    private val PRE_WINDOW_LIMIT_MS = 12_000L
    private val DEFAULT_PRE_MS = 6_000L
    private val DEFAULT_POST_MS = 5_000L

    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var videoCapture: VideoCapture<Recorder>? = null

    // 현재 돌고있는 세그먼트 녹화
    private var currentRecording: Recording? = null
    private var bufferingJob: Job? = null

    // 세그먼트 큐(링버퍼)
    private data class Segment(val file: File, val startMs: Long, var endMs: Long)
    private val segmentQueue: ArrayDeque<Segment> = ArrayDeque()
    private val segMutex = Mutex()

    // 사건 처리
    private data class CaptureRequest(
        val eventTs: Long,
        val preMs: Long,
        val postMs: Long,
        val outFile: File
    )
    @Volatile private var pendingCapture: CaptureRequest? = null
    private var mergingJob: Job? = null

    private class HeadlessSurfaceProvider : Preview.SurfaceProvider {
        private var surfaceTexture: SurfaceTexture? = null
        private var surface: Surface? = null

        override fun onSurfaceRequested(request: SurfaceRequest) {
            val tex = SurfaceTexture(0).apply {
                setDefaultBufferSize(
                    request.resolution.width,
                    request.resolution.height
                )
            }
            val surf = Surface(tex)
            surfaceTexture = tex
            surface = surf

            request.provideSurface(
                surf,
                Runnable::run // 호출 스레드 그대로
            ) {
                // 릴리스
                try { surf.release() } catch (_:Throwable) {}
                try { tex.release() } catch (_:Throwable) {}
            }
        }
    }
    private var lastPreviewViewRef: WeakReference<PreviewView>? = null

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block()
        else Handler(Looper.getMainLooper()).post(block)
    }
    inner class LocalBinder : Binder() {
        fun attachPreview(view: PreviewView) {
            lastPreviewViewRef = WeakReference(view)
            runOnMain {
                preview?.setSurfaceProvider(view.surfaceProvider)
            }
        }
        fun detachPreview() {
            preview?.setSurfaceProvider(HeadlessSurfaceProvider())
        }
        fun startDetection() { startBuffering() }
        fun stopDetection()  { stopBuffering() }
        fun markIncident(preMs: Long, postMs: Long) {
            lifecycleScope.launch { markEvent(preMs, postMs) }
        }
    }

    private val binder = LocalBinder()
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTI_ID, buildNotification("준비 중…"),android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)

        lifecycleScope.launch { initCamera() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> lifecycleScope.launch {
                if (videoCapture == null) initCamera()
                startBuffering() // << 링버퍼 시작
            }
            ACTION_STOP -> {
                stopBuffering()
                stopSelf()
            }
            ACTION_OPEN -> {
                val launch = packageManager.getLaunchIntentForPackage(packageName)
                launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launch)
            }
            ACTION_MARK_EVENT -> {
                val pre = intent.getLongExtra(EXTRA_PRE_MS, DEFAULT_PRE_MS)
                val post = intent.getLongExtra(EXTRA_POST_MS, DEFAULT_POST_MS)
                lifecycleScope.launch { markEvent(pre, post) }
            }
        }
        return START_STICKY
    }

    private suspend fun initCamera() = withContext(Dispatchers.Main) {
        val provider = ProcessCameraProvider.getInstance(this@CameraRecordingService).get()
        cameraProvider = provider

        val qualitySelector = QualitySelector.fromOrderedList(
            listOf(Quality.FHD, Quality.HD, Quality.SD),
            FallbackStrategy.lowerQualityThan(Quality.SD) // 최저 SD까지
        )
        val recorder = Recorder.Builder().setQualitySelector(qualitySelector).build()

        videoCapture = VideoCapture.withOutput(recorder)
        preview = Preview.Builder().build().also {
            // 기본은 헤드리스
            it.setSurfaceProvider(HeadlessSurfaceProvider())
        }

        provider.unbindAll()
        provider.bindToLifecycle(
            this@CameraRecordingService,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            videoCapture
        )

        delay(350)
        lastPreviewViewRef?.get()?.let { pv ->
            preview?.setSurfaceProvider(pv.surfaceProvider)
        }

        updateNotification("준비 완료")
    }

    // ------------------------
    //  링버퍼 세그먼트 녹화 루프
    // ------------------------
    private fun startBuffering() {
        if (bufferingJob?.isActive == true) return
        updateNotification("버퍼링 시작…")

        bufferingJob = lifecycleScope.launch(Dispatchers.Default) {
            while (isActive) {
                val seg = recordOneSegment(SEGMENT_MS) ?: continue
                segMutex.withLock {
                    segmentQueue.addLast(seg)
                    pruneOldSegmentsLocked(keepMs = PRE_WINDOW_LIMIT_MS)
                }
                checkAndMaybeMerge()
            }
        }
    }

    private fun stopBuffering() {
        bufferingJob?.cancel()
        bufferingJob = null
        currentRecording?.stop()
        currentRecording = null
        updateNotification("버퍼링 중지")
        lifecycleScope.launch(Dispatchers.IO) {
            segMutex.withLock {
                segmentQueue.forEach { it.file.delete() }
                segmentQueue.clear()
            }
        }
    }

    /**
     * 5초(설정값)짜리 세그먼트를 하나 녹화하고 파일/시간 정보를 반환
     */
    private suspend fun recordOneSegment(durationMs: Long): Segment? =
        withContext(Dispatchers.Main) {
            val vc = videoCapture ?: return@withContext null

            // 보수적 품질 (FHD->HD->SD 순으로 fallback)
            // initCamera()에서 이미 설정했다면 생략 가능
            // Recorder Builder의 품질은 init에서 해둔 그대로 사용한다고 가정

            val startTs = System.currentTimeMillis()
            val file = File(externalCacheDir ?: cacheDir, "seg_${startTs}.mp4")
            val output = FileOutputOptions.Builder(file).build()

            val startDef = CompletableDeferred<Boolean>()
            val finalizeDef = CompletableDeferred<VideoRecordEvent.Finalize>()

            val rec = vc.output.prepareRecording(this@CameraRecordingService, output)
                // 오디오 쓸 거면 .withAudioEnabled() + FGS type/권한도 추가
                .start(ContextCompat.getMainExecutor(this@CameraRecordingService)) { ev ->
                    when (ev) {
                        is VideoRecordEvent.Start -> {
                            // 실제 인코딩 시작 신호
                            startDef.complete(true)
                        }
                        is VideoRecordEvent.Finalize -> {
                            // start 전에 finalize가 올 수도 있음 -> no valid data 케이스 대부분
                            if (!startDef.isCompleted) startDef.complete(false)
                            finalizeDef.complete(ev)
                        }
                        else -> Unit
                    }
                }

            currentRecording = rec

            // ★ Start 신호 대기 (타임아웃 내 도착 못하면 재시도/포기)
            val started = withTimeoutOrNull(2_500) { startDef.await() } == true
            if (!started) {
                // 초기화 지연/품질 미지원/FGS 미설정 등으로 실패 → 정리 후 리턴
                rec.stop()
                runCatching { finalizeDef.await() }
                currentRecording = null
                file.delete()
                Log.w("Recorder", "Start not received, skip segment (no valid data).")
                return@withContext null
            }

            // 여기서부터만 세그먼트 타이머 카운트
            delay(durationMs)

            rec.stop()
            val fin = finalizeDef.await()
            currentRecording = null

            if (fin.hasError()) {
                // ERROR_NO_VALID_DATA 방지 로직 통과 후에도 에러면 파일 제거
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

    // ------------------------
    //   사건 트리거 & 병합
    // ------------------------
    private suspend fun markEvent(preMs: Long, postMs: Long) {
        val now = System.currentTimeMillis()
        val outDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: filesDir
        val name = "incident_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))}.mp4"
        val outFile = File(outDir, name)

        pendingCapture = CaptureRequest(
            eventTs = now,
            preMs = preMs,
            postMs = postMs,
            outFile = outFile
        )

        updateNotification("사건 감지! 후단 ${postMs/1000}s 수집 중…")
        // 병합은 세그먼트가 충분히 쌓였을 때 checkAndMaybeMerge()가 처리
    }

    private suspend fun checkAndMaybeMerge() {
        val req = pendingCapture ?: return
        // 충분한 세그먼트가 쌓였는지 확인 (event + post 가 포함되는 마지막 세그먼트가 존재해야 함)
        val enough = segMutex.withLock {
            segmentQueue.isNotEmpty() && (segmentQueue.last().endMs >= req.eventTs + req.postMs)
        }
        if (!enough) return

        // 병합에 쓸 파일들 스냅샷
        val sources: List<File> = segMutex.withLock {
            val fromTs = req.eventTs - req.preMs
            val toTs = req.eventTs + req.postMs
            segmentQueue.filter { it.endMs >= fromTs }
                .takeWhile { it.startMs <= toTs }
                .map { it.file }
                .toList()
        }
        if (sources.isEmpty()) return

        // 중복 병합 방지
        if (mergingJob?.isActive == true) return

        mergingJob = lifecycleScope.launch(Dispatchers.Default) {
            try {
                updateNotification("사건 클립 병합 중 (${sources.size}개)…")
                mergeMp4Segments(sources, req.outFile)
                // 갤러리에 보이게 (옵션)
                try {
                    MediaScannerConnection.scanFile(
                        this@CameraRecordingService,
                        arrayOf(req.outFile.absolutePath),
                        arrayOf("video/mp4"),
                        null
                    )
                } catch (_: Throwable) {}

                updateNotification("사건 저장 완료: ${req.outFile.name}")
            } catch (t: Throwable) {
                Log.e("Recorder", "merge error: ${t.message}", t)
                updateNotification("사건 저장 실패")
            } finally {
                pendingCapture = null
                // 병합 이후 오래된 세그먼트 정리
                segMutex.withLock { pruneOldSegmentsLocked(keepMs = PRE_WINDOW_LIMIT_MS) }
            }
        }
    }

    /**
     * 여러 MP4(동일 코덱 파라미터 가정) 파일을 하나의 MP4로 이어붙임.
     * 비디오/오디오 트랙 모두 있으면 둘 다 연결.
     */
    private fun mergeMp4Segments(inputs: List<File>, outFile: File) {
        if (outFile.exists()) outFile.delete()

        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        var dstVideoTrack = -1
        var started = false

        val bufferSize = 2 * 1024 * 1024
        val buffer = ByteBuffer.allocate(bufferSize)
        val info = MediaCodec.BufferInfo()

        var ptsOffsetUs = 0L
        var totalVideoPtsUs = 0L

        fun selectTrack(extractor: MediaExtractor, mimePrefix: String): Int {
            for (i in 0 until extractor.trackCount) {
                val fmt = extractor.getTrackFormat(i)
                val mime = fmt.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith(mimePrefix)) return i
            }
            return -1
        }

        inputs.forEachIndexed { idx, file ->
            val extractor = MediaExtractor()
            extractor.setDataSource(file.absolutePath)

            val srcVid = selectTrack(extractor, "video/")

            if (!started) {
                if (srcVid != -1) {
                    val fmt = extractor.getTrackFormat(srcVid)
                    dstVideoTrack = muxer.addTrack(fmt)
                }
                muxer.start()
                started = true
            }

            // 비디오 먼저
            var lastVideoPts = 0L
            if (srcVid != -1 && dstVideoTrack != -1) {
                extractor.selectTrack(srcVid)
                while (true) {
                    val size = extractor.readSampleData(buffer, 0)
                    if (size < 0) break
                    info.offset = 0
                    info.size = size
                    info.presentationTimeUs = extractor.sampleTime + ptsOffsetUs
                    info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME
                    muxer.writeSampleData(dstVideoTrack, buffer, info)
                    lastVideoPts = info.presentationTimeUs
                    extractor.advance()
                }
                extractor.unselectTrack(srcVid)
            }



            extractor.release()


            totalVideoPtsUs = max(totalVideoPtsUs, lastVideoPts)
            ptsOffsetUs = totalVideoPtsUs + 33_000
        }

        muxer.stop()
        muxer.release()
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
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Foreground camera recording"
            enableLights(false); enableVibration(false)
            lightColor = Color.BLUE
            setSound(null, null)
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(ch)
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun buildNotification(content: String): Notification {
        val stopIntent = Intent(this, CameraRecordingService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, CameraRecordingService::class.java).apply { action = ACTION_OPEN }
        val openPending = PendingIntent.getService(
            this, 1, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
