// presentation/ui/component/violationDetail/ViolationMediaSection.kt
package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import com.sos.chakhaeng.presentation.theme.chakhaengTypography

@Composable
fun ViolationMediaSection(
    videoThumbnailUrl: String?,
    onPlayVideoClick: @Composable () -> Unit,      // ✅ 썸네일 탭 → 재생 콜백
    photoUrls: List<String>,
    modifier: Modifier = Modifier,
    cardShape: Shape = RoundedCornerShape(16.dp)
) {
    ElevatedCard(modifier = modifier.fillMaxWidth(), shape = cardShape) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("사진/동영상", style = chakhaengTypography().bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            if (videoThumbnailUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16/9f)
                        .clip(cardShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = videoThumbnailUrl,
                        contentDescription = "위반 동영상 썸네일",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    FilledTonalIconButton(onClick = onPlayVideoClick as () -> Unit) {
                        Icon(Icons.Outlined.PlayCircle, contentDescription = "재생")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (photoUrls.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(photoUrls) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "증거 사진",
                            modifier = Modifier
                                .width(160.dp)
                                .height(100.dp)
                                .clip(cardShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                Text("등록된 사진이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
