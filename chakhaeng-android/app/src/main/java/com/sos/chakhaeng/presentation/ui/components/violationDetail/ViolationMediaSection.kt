// presentation/ui/component/violationDetail/ViolationMediaSection.kt
package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.media3.common.MimeTypes
import com.sos.chakhaeng.presentation.theme.ViolationColors.HighIcon
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.errorLight
import com.sos.chakhaeng.presentation.theme.onSurfaceVariantLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.ui.screen.streaming.component.VideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViolationMediaSection(
    videoUrl: String,                           // ✅ 비디오 URL만 받음
    modifier: Modifier = Modifier,
    cardShape: Shape = RoundedCornerShape(16.dp),
    autoPlay: Boolean = false,
    showControls: Boolean = false,
    initialMute: Boolean = false,
    onRequestUpload: () -> Unit = {},
    onRequestEdit: () -> Unit = {},
    onRequestDelete: () -> Unit = {},
) {
    // 바텀 시트 관리
    var openSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ElevatedCard(modifier = modifier.fillMaxWidth(), shape = cardShape) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "동영상",
                    style = chakhaengTypography().bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceVariantLight
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { openSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = primaryLight
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "영상 관리",
                        style = chakhaengTypography().bodyMedium,
                        color = primaryLight
                    )
                }
            }
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

    if (openSheet) {
        ModalBottomSheet(
            onDismissRequest = { openSheet = false },
            sheetState = sheetState,
        ) {
            if (videoUrl.isNotBlank()) {
                ActionItem(
                    icon = { Icon(Icons.Default.Edit, contentDescription = null)},
                    title = "동영상 수정",
                    onClick = {
                        openSheet = false
                        onRequestEdit()
                    }
                )
                ActionItem(
                    icon = { Icon(Icons.Default.Delete, contentDescription = null)},
                    title = "동영상 삭제",
                    isDelete = true,
                    onClick = {
                        openSheet = false
                        onRequestDelete()
                    }
                )
            } else {
                ActionItem(
                    icon = { Icon(Icons.Outlined.CloudUpload, contentDescription = null)},
                    title = "동영상 업로드",
                    onClick = {
                        openSheet = false
                        onRequestUpload()
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActionItem(
    icon: @Composable () -> Unit,
    title: String,
    isDelete: Boolean = false,
    onClick: () -> Unit
) {
    val colors = if (isDelete) {
        ListItemDefaults.colors(
            headlineColor = HighIcon,
            leadingIconColor = HighIcon
        )
    } else {
        ListItemDefaults.colors()
    }
    ListItem(
        leadingContent = icon,
        headlineContent = { Text(title) },
        colors = colors,
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple { onClick() }
    )
}

@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    composed {
        clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    }
