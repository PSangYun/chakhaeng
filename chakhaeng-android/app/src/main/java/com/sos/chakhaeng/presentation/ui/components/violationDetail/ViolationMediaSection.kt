// presentation/ui/component/violationDetail/ViolationMediaSection.kt
package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.media3.common.MimeTypes
import com.sos.chakhaeng.presentation.ui.screen.streaming.component.VideoPlayer

@Composable
fun ViolationMediaSection(
    videoUrl: String,                           // ✅ 비디오 URL만 받음
    modifier: Modifier = Modifier,
    cardShape: Shape = RoundedCornerShape(16.dp),
    autoPlay: Boolean = false,
    showControls: Boolean = false,
    initialMute: Boolean = false,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth(), shape = cardShape) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                "동영상",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            if (!videoUrl.isNullOrBlank()) {
                val mime = remember(videoUrl) {
                    when {
                        videoUrl.endsWith(".m3u8", true) -> MimeTypes.APPLICATION_M3U8
                        videoUrl.endsWith(".mpd", true)  -> MimeTypes.APPLICATION_MPD
                        videoUrl.endsWith(".mp4", true)  -> MimeTypes.VIDEO_MP4
                        else -> null                     // VideoPlayer 내부 기본 탐지 사용
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(cardShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    VideoPlayer(
                        modifier = Modifier.fillMaxSize(),
                        url = videoUrl,
                        mimeType = mime,
                        autoPlay = autoPlay,
                        useController = showControls,
                        initialMute = initialMute,
                        onBackFromFullscreen = { /* no-op */ }
                    )
                }
            } else {
                // 빈 상태
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(cardShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "동영상이 없습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
