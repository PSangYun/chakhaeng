package com.sos.chakhaeng.presentation.ui.screen.streaming.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
    onToggleFullscreen: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LargeIconButton(onClick = onSeekBack, icon = Icons.Default.Replay10, contentDesc = "뒤로 10초")
            LargeIconButton(
                onClick = onPlayPause,
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDesc = if (isPlaying) "일시정지" else "재생",
                emphasized = true
            )
            LargeIconButton(onClick = onSeekForward, icon = Icons.Default.Forward10, contentDesc = "앞으로 10초")
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
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
                    Text(
                        text = "${formatMs(positionMs)} / ${formatMs(durationMs)}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.weight(1F))

                    Icon(Icons.Default.Fullscreen, contentDescription = "전체화면", tint = Color.White, modifier = Modifier.clickable{
                        onToggleFullscreen()
                    })
                }

            } else {
                Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}