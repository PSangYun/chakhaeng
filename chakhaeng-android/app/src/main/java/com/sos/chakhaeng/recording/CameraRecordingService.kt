package com.sos.chakhaeng.recording

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.graphics.Color
import android.os.*
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.video.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CameraRecordingService : LifecycleService() {

    companion object {
        const val CHANNEL_ID = "record_cam_channel"
        const val NOTI_ID = 7771
        const val ACTION_START = "com.sos.chakhaeng.action.START_RECORDING"
        const val ACTION_STOP  = "com.sos.chakhaeng.action.STOP_RECORDING"
        const val ACTION_OPEN  = "com.sos.chakhaeng.action.OPEN_APP"
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTI_ID, buildNotification("준비 중…"))

        // 카메라 준비
        lifecycleScope.launch {
            initCamera()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> lifecycleScope.launch {
                if (videoCapture == null) initCamera()
                startRecording()
            }
            ACTION_STOP -> {
                stopRecording()
                stopSelf()
            }
            ACTION_OPEN -> {
                val launch = packageManager.getLaunchIntentForPackage(packageName)
                launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launch)
            }
        }
        return START_STICKY
    }

    private suspend fun initCamera() = withContext(Dispatchers.Main) {
        val provider = ProcessCameraProvider.getInstance(this@CameraRecordingService).get()
        cameraProvider = provider

        val qualitySelector = QualitySelector.from(
            Quality.FHD,
            FallbackStrategy.higherQualityOrLowerThan(Quality.FHD)
        )
        val recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        provider.unbindAll()
        provider.bindToLifecycle(
            /* lifecycleOwner = */ this@CameraRecordingService,
            CameraSelector.DEFAULT_BACK_CAMERA,
            videoCapture
        )
        updateNotification("준비 완료")
    }

    @OptIn(ExperimentalPersistentRecording::class)
    private fun startRecording() {
        if (recording != null) return

        val name = "VID_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}"

        val outputOptions: OutputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ChakHaeng")
            }
            MediaStoreOutputOptions.Builder(
                contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
                .setContentValues(contentValues)
                .build()
        } else {
            val dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            val file = java.io.File(dir, "$name.mp4")
            FileOutputOptions.Builder(file).build()
        }

        val rec = videoCapture?.output
            ?.prepareRecording(this, outputOptions as MediaStoreOutputOptions)
            ?.start(ContextCompat.getMainExecutor(this)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> updateNotification("녹화 중…")
                    is VideoRecordEvent.Status -> {
                        // 진행률/비트레이트 등 필요하면 활용
                    }
                    is VideoRecordEvent.Finalize -> {
                        val msg = if (event.hasError())
                            "녹화 오류: ${event.error}"
                        else
                            "녹화 완료"
                        updateNotification(msg)
                    }
                }
            }

        recording = rec
    }

    private fun stopRecording() {
        recording?.stop()
        recording = null
        updateNotification("녹화 중지")
    }

    override fun onDestroy() {
        super.onDestroy()
        recording?.close()
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
        val stopIntent = Intent(this, CameraRecordingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, CameraRecordingService::class.java).apply {
            action = ACTION_OPEN
        }
        val openPending = PendingIntent.getService(
            this, 1, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setContentTitle("ChakHaeng 녹화")
            .setContentText(content)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "중지",
                stopPending
            )
            .setContentIntent(openPending)
            .setForegroundServiceBehavior(
                NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            )
            .build()
    }

    private fun updateNotification(content: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTI_ID, buildNotification(content))
    }
}
