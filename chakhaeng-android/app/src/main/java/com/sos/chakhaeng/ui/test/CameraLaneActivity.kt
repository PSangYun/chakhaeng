package com.sos.chakhaeng.ui.test

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import androidx.activity.ComponentActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.sos.chakhaeng.core.ai.LaneDetector
import com.sos.chakhaeng.core.ai.LaneModelSpec
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors

private const val TAG = "bandi2"

class CameraLaneActivity : ComponentActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView
    private lateinit var laneDetector: LaneDetector
    private lateinit var interpreter: Interpreter
    private val analysisExecutor = Executors.newSingleThreadExecutor()

    @Volatile private var isDetecting = false
    @Volatile private var latestLanes: List<List<Triple<Int, Int, Float>>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CameraX Preview
        previewView = PreviewView(this).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
        overlayView = OverlayView(this)

        // Preview + OverlayView 겹치기
        val container = ConstraintLayout(this).apply {
            addView(previewView, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
            addView(overlayView, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        }
        setContentView(container)

        // 모델 스펙
        val spec = LaneModelSpec(
            key = "lane_v2",
            assetPath = "models/culane_res18_dynamic.tflite",
            preferInputW = 1600,
            preferInputH = 320,
            numRow = 72,
            numCol = 81,
            numCellRow = 200,
            numCellCol = 100,
            numLanes = 4,
            cropRatio = 0.6f
        )

        // Interpreter 초기화
        val modelBuffer = FileUtil.loadMappedFile(this, spec.assetPath)
        val nnApiDelegate = NnApiDelegate()
        val options = Interpreter.Options().apply { addDelegate(nnApiDelegate) }
        interpreter = Interpreter(modelBuffer, options)
        laneDetector = LaneDetector(interpreter, spec)

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // ✅ Preview와 Analysis 해상도 통일
            val analysisResolution = Size(1920, 1080)

            val preview = Preview.Builder()
                .setTargetResolution(analysisResolution)
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(analysisResolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(analysisExecutor) { image ->
                        processFrame(image)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Camera binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processFrame(image: ImageProxy) {
        try {
            val bmp = image.toBitmap()
            image.close()

            // ✅ OverlayView에 스케일링 적용
            latestLanes?.let { lanes ->
                runOnUiThread {
                    overlayView.updateLanes(bmp.width, bmp.height, lanes)
                }
            }

            if (isDetecting) return

            isDetecting = true
            analysisExecutor.execute {
                try {
                    val start = System.nanoTime()
                    val lanes = laneDetector.detect(bmp)
                    val end = System.nanoTime()
                    Log.d(TAG, "Inference time = ${(end - start) / 1e6} ms")

                    latestLanes = lanes
                } catch (t: Throwable) {
                    Log.e(TAG, "detect failed: ${t.message}")
                } finally {
                    isDetecting = false
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "processFrame error: ${t.message}", t)
            runCatching { image.close() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { interpreter.close() }
        analysisExecutor.shutdown()
    }
}

/** 🔹 OverlayView */
class OverlayView(context: Context) : View(context) {
    private var lanes: List<List<Triple<Int, Int, Float>>>? = null
    private var frameW: Int = 1
    private var frameH: Int = 1

    fun updateLanes(frameWidth: Int, frameHeight: Int, lanes: List<List<Triple<Int, Int, Float>>>) {
        this.frameW = frameWidth
        this.frameH = frameHeight
        this.lanes = lanes
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        lanes?.let { laneList ->
            val colors = arrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)

            val scaleX = width.toFloat() / frameW.toFloat()
            val scaleY = height.toFloat() / frameH.toFloat()

            for ((laneIdx, lane) in laneList.withIndex()) {
                for (i in 0 until lane.size - 1) {
                    val (x1, y1, c1) = lane[i]
                    val (x2, y2, c2) = lane[i + 1]
                    val confidence = ((c1 + c2) / 2f).coerceIn(0f, 1f)

                    val paint = Paint().apply {
                        color = colors[laneIdx % colors.size]
                        strokeWidth = 4f + confidence * 6f
                        alpha = (80 + confidence * 175).toInt()
                        style = Paint.Style.STROKE
                        isAntiAlias = true
                    }

                    canvas.drawLine(
                        x1 * scaleX,
                        y1 * scaleY,
                        x2 * scaleX,
                        y2 * scaleY,
                        paint
                    )
                }
            }
        }
    }
}

/** 🔹 YUV420 → Bitmap 변환 */
fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer: ByteBuffer = planes[0].buffer
    val uBuffer: ByteBuffer = planes[1].buffer
    val vBuffer: ByteBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()

    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}
