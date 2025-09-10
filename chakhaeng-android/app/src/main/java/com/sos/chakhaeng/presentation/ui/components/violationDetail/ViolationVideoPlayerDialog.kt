package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sos.chakhaeng.presentation.ui.screen.streaming.component.VideoPlayer

@Composable
fun ViolationVideoPlayerDialog(
    url: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // ✅ 풀스크린
    ) {
        // 배경/여백 없이 그대로 플레이어만
        Surface(tonalElevation = 0.dp, shadowElevation = 0.dp) {
            VideoPlayer(
                modifier = Modifier, // 내부에서 fillMaxWidth/Size 처리함
                url = url,
                useController = false,
                autoPlay = true,
                initialMute = false,
                rotateOnFullscreen = true,
                onBackFromFullscreen = onDismiss
            )
        }
    }
}