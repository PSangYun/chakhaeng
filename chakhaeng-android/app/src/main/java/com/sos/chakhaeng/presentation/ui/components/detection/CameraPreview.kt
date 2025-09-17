package com.sos.chakhaeng.presentation.ui.components.detection

import android.view.TextureView
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    controller: LifecycleCameraController,
    textureView: TextureView
) {
    val context = LocalContext.current
    AndroidView(
        factory = { textureView },
        modifier = modifier,
    )
}