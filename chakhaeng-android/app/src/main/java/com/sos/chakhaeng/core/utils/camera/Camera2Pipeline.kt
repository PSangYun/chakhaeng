package com.sos.chakhaeng.core.utils.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import android.view.Surface
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class Camera2Pipeline(
    private val context: Context,
    private val cameraId: String,
    private val previewSurface: Surface,
    private val analysisReader: ImageReader,
    private val encoderSurface: Surface,
    private val targetFps: Int = 30
) {
    @SuppressLint("ServiceCast")
    private val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private lateinit var camera : CameraDevice
    private lateinit var session: CameraCaptureSession
    private val bgThread = HandlerThread("cam-bg").apply { start() }
    private val bgHandler = Handler(bgThread.looper)

    @RequiresPermission(android.Manifest.permission.CAMERA)
    suspend fun start() {
        ensureCameraPermission()

        camera = openCamera()
        session = createSession()
        val req = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            addTarget(previewSurface)
            addTarget(analysisReader.surface)
            addTarget(encoderSurface)
            set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(targetFps, targetFps))
        }.build()
        Log.d("Camera2Pipeline", "start: $req")
        session.setRepeatingRequest(req, null, bgHandler)
    }

    fun stop() {
        runCatching {
            session.stopRepeating()
        }
        runCatching {
            session.abortCaptures()
        }
        runCatching {
            session.close()
        }
        runCatching {
            camera.close()
        }
        runCatching { analysisReader.setOnImageAvailableListener(null, null) }
        runCatching { analysisReader.close() }
        bgThread.quitSafely()
    }

    private fun ensureCameraPermission() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted) throw SecurityException("CAMERA permission not granted")
    }

    private suspend fun openCamera(): CameraDevice =
        suspendCancellableCoroutine { cont ->
            val resumed = AtomicBoolean(false)

            val cb = object : CameraDevice.StateCallback() {
                override fun onOpened(device: CameraDevice) {
                    if (resumed.compareAndSet(false, true)) {
                        cont.resume(device)
                    } else {
                        runCatching { device.close() }
                    }

                }

                override fun onDisconnected(device: CameraDevice) {
                    if (resumed.compareAndSet(false, true)) {
                        cont.resumeWithException(RuntimeException("Camera disconnected"))
                    }
                    runCatching { device.close() }
                }

                override fun onError(device: CameraDevice, error: Int) {
                    if (resumed.compareAndSet(false, true)) {
                        cont.resumeWithException(RuntimeException("Camera error: $error"))
                    }
                    runCatching { device.close() }

                }
            }
            manager.openCamera(cameraId, cb, bgHandler)

            cont.invokeOnCancellation {

            }
        }

    private suspend fun createSession(): CameraCaptureSession =
        suspendCancellableCoroutine { cont ->
            val resumed = AtomicBoolean(false)
            val targets = listOf(previewSurface, analysisReader.surface, encoderSurface)
            camera.createCaptureSession(
                targets,
                object: CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(cs: CameraCaptureSession) {
                        if (resumed.compareAndSet(false, true)) {
                            cont.resumeWithException(RuntimeException("Session configure failed"))
                        }
                    }

                    override fun onConfigured(cs: CameraCaptureSession) {
                        if (resumed.compareAndSet(false, true)) {
                            cont.resume(cs)
                        }
                    }
                },
                bgHandler
            )
            cont.invokeOnCancellation {  }
        }
}