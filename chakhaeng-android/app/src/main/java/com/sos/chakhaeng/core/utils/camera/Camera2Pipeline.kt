package com.sos.chakhaeng.core.utils.camera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Range
import android.view.Surface
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.suspendCancellableCoroutine
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
        camera = openCamera()
        session = createSession()
        val req = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            addTarget(previewSurface)
            addTarget(analysisReader.surface)
            addTarget(encoderSurface)
            set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(targetFps, targetFps))
        }.build()
        session.setRepeatingRequest(req, null, bgHandler)
    }

    fun stop() {
        runCatching {
            session.close()
        }
        runCatching {
            camera.close()
        }
        bgThread.quitSafely()
        analysisReader.close()

    }

    private suspend fun openCamera(): CameraDevice =
        suspendCancellableCoroutine { cont ->
            val cb = object : CameraDevice.StateCallback() {
                override fun onOpened(device: CameraDevice) {
                    cont.resume(device)
                }

                override fun onDisconnected(device: CameraDevice) {
                    cont.resumeWithException(RuntimeException("Camera disconnected"))
                }

                override fun onError(device: CameraDevice, error: Int) {
                    cont.resumeWithException(RuntimeException("Camera error: $error"))

                }
            }
            manager.openCamera(cameraId, cb, bgHandler)
        }

    private suspend fun createSession(): CameraCaptureSession =
        suspendCancellableCoroutine { cont ->
            val targets = listOf(previewSurface, analysisReader.surface, encoderSurface)
            camera.createCaptureSession(
                targets,
                object: CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(cs: CameraCaptureSession) {
                        cont.resumeWithException(RuntimeException("Session configure failed"))
                    }

                    override fun onConfigured(cs: CameraCaptureSession) {
                        cont.resume(cs)
                    }
                },
                bgHandler
            )
        }
}