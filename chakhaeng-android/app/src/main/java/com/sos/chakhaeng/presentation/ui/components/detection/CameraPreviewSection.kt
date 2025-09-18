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
    onAnalyzeFrame: (Bitmap, Int) -> Unit
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
                val bmp = image.toBitmap(yuv)
                val rotation = image.imageInfo.rotationDegrees // 0,90,180,270
                onAnalyzeFrame(bmp, rotation)
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