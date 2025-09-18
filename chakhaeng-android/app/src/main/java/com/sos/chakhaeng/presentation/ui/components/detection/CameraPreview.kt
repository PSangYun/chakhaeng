package com.sos.chakhaeng.presentation.ui.components.detection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.sos.chakhaeng.recording.CameraRecordingService

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onBound: ((CameraRecordingService.LocalBinder) -> Unit)? = null
) {
    val context = LocalContext.current
    val previewView = remember {
        androidx.camera.view.PreviewView(context).apply {
            scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
            implementationMode = androidx.camera.view.PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    var binder by remember { mutableStateOf<CameraRecordingService.LocalBinder?>(null) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                binder = service as? CameraRecordingService.LocalBinder
                // 바인드되면 화면에 프리뷰 "꽂기"
                binder?.attachPreview(previewView)
                onBound?.invoke(binder!!)
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                binder = null
            }
        }
    }

    // 바인드/언바인드 생명주기
    DisposableEffect(Unit) {
        val intent = Intent(context, CameraRecordingService::class.java)
        // 서비스가 꺼져 있다면 FGS로 띄우고 싶으면 startForegroundService 먼저 호출해도 됨
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        onDispose {
            runCatching { binder?.detachPreview() }
            context.unbindService(connection)
        }
    }

    // 실제 뷰
    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}