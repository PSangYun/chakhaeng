package com.sos.chakhaeng.presentation.ui.components.detection

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sos.chakhaeng.core.ai.Detection
import com.sos.chakhaeng.core.camera.YuvToRgbConverter
import com.sos.chakhaeng.core.camera.toBitmap
import java.util.concurrent.Executors

@Composable
fun CameraPreviewSection(
    isDetectionActive: Boolean,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
    controller: LifecycleCameraController,
    detection: List<Detection>,
    onAnalyzeFrame: (Bitmap, Int) -> Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // YUV -> Bitmap 변환기 & 분석 전용 쓰레드
    val yuv = remember { YuvToRgbConverter(context) }
    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }

    // Analyzer 바인딩/해제
    DisposableEffect(controller, isDetectionActive) {
        controller.setEnabledUseCases(CameraController.IMAGE_ANALYSIS or CameraController.VIDEO_CAPTURE)
        controller.setImageAnalysisBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)


        if (isDetectionActive) {
            controller.setImageAnalysisAnalyzer(analyzerExecutor) { image ->
                try {
                    val bmp = image.toBitmap(yuv)        // YUV → Bitmap
                    val rotation = image.imageInfo.rotationDegrees
                    // (선택) 프레임 유입 로그
                    android.util.Log.d("DTAG", "analyzer frame in, rot=$rotation, ${bmp.width}x${bmp.height}")
                    val accepted = onAnalyzeFrame(bmp, rotation)

                    if (!accepted) {
                        // 스킵된 프레임은 analyzer에서 즉시 정리
                        if (!bmp.isRecycled) bmp.recycle()
                    }
                } catch (t: Throwable) {
                    android.util.Log.e("DTAG", "analyzer error: ${t.message}", t)
                } finally {
                    // ✅ 꼭 닫아줘야 다음 프레임이 들어옵니다
                    image.close()
                }
            }
        } else {
            controller.clearImageAnalysisAnalyzer()
        }

        onDispose {
            controller.clearImageAnalysisAnalyzer()
            analyzerExecutor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            controller = controller
        )

        DetectionOverlay(
            modifier = Modifier.fillMaxSize(),
            detections = detection
        )

        CameraStatusOverlay(
            isDetectionActive = isDetectionActive,
            isFullscreen = isFullscreen,
            onToggleFullscreen = onToggleFullscreen,
            modifier = Modifier.fillMaxSize()
        )
    }
}