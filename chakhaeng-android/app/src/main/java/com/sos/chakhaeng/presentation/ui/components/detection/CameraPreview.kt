package com.sos.chakhaeng.presentation.ui.components.detection

import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onCameraReady: (Camera) -> Unit = {},
    onImageCaptured: (ImageProxy) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // 카메라 실행자
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    ) { view ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        // AI 분석을 위한 이미지 프레임 전달
                        onImageCaptured(imageProxy)
                        imageProxy.close()
                    }
                }

            // 카메라 선택 (후면 카메라)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 기존 바인딩 해제
                provider.unbindAll()

                // 카메라와 UseCase 바인딩
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

                // 카메라 준비 완료 콜백
                camera?.let { onCameraReady(it) }

            } catch (exc: Exception) {
                Log.e("CameraPreview", "카메라 바인딩 실패", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    // 컴포저블이 제거될 때 정리
    DisposableEffect(Unit) {
        onDispose {
            try {
                Log.d("CameraPreview", "카메라 리소스 정리 시작")

                cameraProvider?.unbindAll()
                cameraExecutor.shutdown()

                Log.d("CameraPreview", "카메라 리소스 정리 완료")
            } catch (e: Exception) {
                Log.e("CameraPreview", "정리 중 오류", e)
            }
        }
    }
}