package com.sos.chakhaeng.presentation.ui.screen.streaming.component

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.ui.screen.streaming.component.util.formatMs

@Composable
fun ControlOverlay(
    isPlaying: Boolean,
    durationMs: Long,
    positionMs: Long,
    onPlayPause: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onScrubStart: () -> Unit,
    onScrubEnd: () -> Unit,
    onToggleFullscreen: () -> Unit,
    forcePortraitUi: Boolean
) {
    val conf = LocalConfiguration.current
    val isPortraitUi = forcePortraitUi ||
            conf.orientation == Configuration.ORIENTATION_PORTRAIT
    if (isPortraitUi) {
        Box(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LargeIconButton(
                    onClick = onSeekBack,
                    icon = Icons.Default.Replay10,
                    contentDesc = "뒤로 10초"
                )
                LargeIconButton(
                    onClick = onPlayPause,
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDesc = if (isPlaying) "일시정지" else "재생",
                    emphasized = forcePortraitUi
                )
                LargeIconButton(
                    onClick = onSeekForward,
                    icon = Icons.Default.Forward10,
                    contentDesc = "앞으로 10초"
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (durationMs > 0L) {
                    var sliderPos by remember(positionMs, durationMs) {
                        mutableFloatStateOf(positionMs.coerceIn(0, durationMs).toFloat())
                    }
                    SeekBarStyled(
                        durationMs = durationMs,
                        positionMs = positionMs,
                        bufferedMs = 0L,
                        onSeekTo = { ms -> onSeekTo(ms) },
                        onScrubStart = onScrubStart,
                        onScrubEnd = onScrubEnd
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatMs(positionMs)} / ${formatMs(durationMs)}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.weight(1F))

                        Icon(
                            Icons.Default.Fullscreen,
                            contentDescription = "전체화면",
                            tint = Color.White,
                            modifier = Modifier.clickable {
                                onToggleFullscreen()
                            })
                    }

                } else {
                    Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
@Composable
internal fun PortraitLockedOverlay(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val conf = LocalConfiguration.current
    BoxWithConstraints(modifier.fillMaxSize()) {
        if (conf.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 컨테이너 W×H 안에 90도 회전된 세로 UI가 꽉 차도록 스케일
            val W = maxWidth.value
            val H = maxHeight.value
            val scale = if (W > 0f) H / W else 1f   // 세로높이(H)에 맞추는 스케일

            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 필요시 -90f로 바꿔 방향 반전
                        rotationZ = -90f
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    },
                content = content
            )
        } else {
            Box(Modifier.fillMaxSize(), content = content) // 포트레이트에선 그대로
        }
    }
}
