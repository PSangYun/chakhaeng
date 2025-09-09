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

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onCameraReady: (Camera) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }
    }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    ) { view ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(view.surfaceProvider)
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                provider.unbindAll()

                val camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )

                Log.d("CameraPreview", "카메라 바인딩 성공")
                onCameraReady(camera)

            } catch (exc: Exception) {
                Log.e("CameraPreview", "카메라 바인딩 실패", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
        }
    }
}