package com.sos.chakhaeng.presentation.ui.components.detection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.sos.chakhaeng.recording.CameraRecordingService
import kotlinx.coroutines.delay

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var binder by remember { mutableStateOf<CameraRecordingService.LocalBinder?>(null) }

    // 서비스 시작
    LaunchedEffect(Unit) {
        ContextCompat.startForegroundService(
            context,
            Intent(context, CameraRecordingService::class.java).apply {
                action = CameraRecordingService.ACTION_START
            }
        )
    }

    // 바인드
    DisposableEffect(Unit) {
        val conn = object : ServiceConnection {
            override fun onServiceConnected(n: ComponentName?, s: IBinder?) {
                binder = s as? CameraRecordingService.LocalBinder
            }
            override fun onServiceDisconnected(n: ComponentName?) { binder = null }
        }
        context.bindService(Intent(context, CameraRecordingService::class.java), conn, Context.BIND_AUTO_CREATE)
        onDispose { runCatching { context.unbindService(conn) } }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = modifier,
        update = { pv ->
            // 매 프레임 호출돼도 안전: 항상 ‘재바인딩’이라 레이스가 사라짐
            binder?.attachPreview(pv)
        }
    )
}

