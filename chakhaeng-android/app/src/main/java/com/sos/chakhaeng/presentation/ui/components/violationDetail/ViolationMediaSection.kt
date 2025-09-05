package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.foundation.ExperimentalFoundationApi
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
import coil.compose.AsyncImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViolationMediaSection(
    videoThumbnailUrl: String?,         // 동영상 썸네일(없으면 null)
    onPlayVideoClick: () -> Unit,       // 동영상 재생 콜백
    photoUrls: List<String>,            // 증거 사진 리스트
    onPhotoClick: (index: Int) -> Unit, // 사진 클릭(확대/뷰어 등)
    modifier: Modifier = Modifier,
    cardShape: Shape = RoundedCornerShape(16.dp)
) {
    ElevatedCard(modifier = modifier.fillMaxWidth(), shape = cardShape) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "사진/동영상",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            // 동영상
            if (videoThumbnailUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
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
                    FilledTonalIconButton(onClick = onPlayVideoClick) {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircle,
                            contentDescription = "재생"
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 사진 썸네일 가로 스크롤
            if (photoUrls.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(photoUrls.indices.toList()) { idx ->
                        AsyncImage(
                            model = photoUrls[idx],
                            contentDescription = "증거 사진 ${idx + 1}",
                            modifier = Modifier
                                .width(160.dp)
                                .height(100.dp)
                                .clip(cardShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                Text(
                    text = "등록된 사진이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
