package com.sos.chakhaeng.core.utils.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.ImageReader
import android.view.Surface
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.sos.chakhaeng.core.utils.camera.Camera2Pipeline
import com.sos.chakhaeng.core.utils.camera.RollingEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class DetectionCameraEngine(
    private val context: Context,
    private val width: Int = 1280,
    private val height: Int = 720,
    private val fps: Int = 30,
    private val iFrameSec: Int = 2,
    private val maxBufferSec: Int = 30
) {
    private var rolling: RollingEncoder? = null
    private var pipeline: Camera2Pipeline? = null
    private var analysisReader: ImageReader? = null
    private var headless: HeadlessPreview? = null
    private val started = AtomicBoolean(false)

    /** 프레임 분석 콜백(온디바이스 AI 연결 지점). true를 반환하면 “감지됨” 의미 */
    var analyzer: (suspend (Image) -> Boolean)? = null

    /** UI: previewSurface 전달 / 서비스: null 전달(Headless Surface 내부 생성) */
    @RequiresPermission(Manifest.permission.CAMERA)
    suspend fun start(previewSurface: Surface?) {
        if (started.compareAndSet(false, true).not()) return

        // 권한 가드
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        check(granted) { "CAMERA permission not granted" }

        withContext(Dispatchers.Default) {
            // 1) 인코더
            val enc = RollingEncoder(context, width, height, fps, iFrameSec, bitrate = width * height * 4, maxBufferSec = maxBufferSec)
            enc.start()
            rolling = enc

            // 2) 프리뷰 Surface (없으면 Headless)
            val preview: Surface = previewSurface ?: run {
                val hp = HeadlessPreview(width, height).also { it.create() }
                headless = hp
                hp.surface!!
            }

            // 3) 분석 리더 + 콜백
            val reader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 2)
            analysisReader = reader
            reader.setOnImageAvailableListener({ r ->
                r.acquireLatestImage()?.use { image ->
                    analyzer?.let { block ->
                        // 외부에서 감지 true면 파일 캡처/업로드는 호출측에서 진행
                        // (원하면 엔진 내부에서 바로 captureClip까지 해도 됨)
                    }
                }
            }, null)

            // 4) 파이프라인
            val p = Camera2Pipeline(
                context = context,
                cameraId = chooseBackCamera(),
                previewSurface = preview,
                analysisReader = reader,
                encoderSurface = enc.inputSurface,
                targetFps = fps
            )
            pipeline = p
            p.start()
        }
    }

    suspend fun stop() {
        if (started.compareAndSet(true, false).not()) return
        withContext(Dispatchers.Default) {
            runCatching { pipeline?.stop() }; pipeline = null
            runCatching { analysisReader?.setOnImageAvailableListener(null, null) }
            runCatching { analysisReader?.close() }; analysisReader = null
            runCatching { rolling?.stop() }; rolling = null
            runCatching { headless?.release() }; headless = null
        }
    }

    /** 감지 시점에서 20초 파일 뽑기 */
    suspend fun captureClip(preSec: Int = 10, postSec: Int = 10) =
        rolling?.captureClip(preSec, postSec)
            ?: throw IllegalStateException("Engine not started")

    private fun chooseBackCamera(): String {
        val mgr = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return mgr.cameraIdList.first { id ->
            val c = mgr.getCameraCharacteristics(id)
            c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }
    }
}
