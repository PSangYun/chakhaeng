package com.sos.chakhaeng.presentation.ui.components.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.media3.common.MimeTypes
import com.sos.chakhaeng.presentation.ui.screen.streaming.component.VideoPlayer

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun ReportMediaSection(
    videoUrl: String,
    modifier: Modifier = Modifier,
    cardShape: Shape = RoundedCornerShape(16.dp),
    autoPlay: Boolean = false,
    showControls: Boolean = false,
    initialMute: Boolean = false,
) {
    if (!videoUrl.isNullOrBlank()) {
        val mime = remember(videoUrl) {
            when {
                videoUrl.endsWith(".m3u8", true) -> MimeTypes.APPLICATION_M3U8
                videoUrl.endsWith(".mpd", true)  -> MimeTypes.APPLICATION_MPD
                videoUrl.endsWith(".mp4", true)  -> MimeTypes.VIDEO_MP4
                else -> null
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