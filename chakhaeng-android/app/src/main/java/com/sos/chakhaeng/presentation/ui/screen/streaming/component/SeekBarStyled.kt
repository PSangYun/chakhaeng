package com.sos.chakhaeng.presentation.ui.screen.streaming.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekBarStyled(
    durationMs: Long,
    positionMs: Long,
    bufferedMs: Long,                  // 없으면 0L로
    onSeekTo: (Long) -> Unit,
    onScrubStart: () -> Unit,
    onScrubEnd: () -> Unit,
) {
    val ytRed = Color(0xFFFF3D00)
    val baseTrack = Color.White.copy(alpha = 0.24f)     // 바탕 라인
    val bufferedTrack = Color.White.copy(alpha = 0.55f) // 버퍼 라인

    var sliderPos by remember(positionMs, durationMs) {
        mutableStateOf(positionMs.coerceIn(0, durationMs).toFloat())
    }
    val dur = durationMs.coerceAtLeast(1L).toFloat()
    val bufferedFrac = (if (durationMs > 0) bufferedMs.toFloat() / durationMs else 0f)
        .coerceIn(0f, 1f)

    Box(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {


        // 실제 슬라이더 (active만 보이게, 나머지는 투명)
        Slider(
            value = sliderPos,
            onValueChange = {
                sliderPos = it
                onScrubStart()
            },
            onValueChangeFinished = {
                onSeekTo(sliderPos.toLong())
                onScrubEnd()
            },
            valueRange = 0f..dur,
            modifier = Modifier.fillMaxWidth().height(3.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,                 // ✅ thumb 보이게
                activeTrackColor = ytRed,                 // 진행 구간
                inactiveTrackColor = Color.Transparent,   // 우리가 그린 바탕/버퍼가 보이도록
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
                disabledActiveTrackColor = ytRed.copy(alpha = 0.38f),
                disabledInactiveTrackColor = Color.Transparent,
                disabledThumbColor = Color.White.copy(alpha = 0.38f),
            ),
            thumb = {
                Box(
                    Modifier
                        .size(18.dp)
                        .background(Color.White, CircleShape)
                )
            },
            track = { sliderState ->

                // Calculate fraction of the slider that is active
                val fraction by remember {
                    derivedStateOf {
                        (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                    }
                }

                Box(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth(fraction)
                            .align(Alignment.CenterStart)
                            .height(6.dp)
                            .padding(end = 16.dp)
                            .background(Color.Yellow, CircleShape)
                    )
                    Box(
                        Modifier
                            .fillMaxWidth(1f - fraction)
                            .align(Alignment.CenterEnd)
                            .height(1.dp)
                            .padding(start = 16.dp)
                            .background(Color.White, CircleShape)
                    )
                }
            }
        )
    }
}