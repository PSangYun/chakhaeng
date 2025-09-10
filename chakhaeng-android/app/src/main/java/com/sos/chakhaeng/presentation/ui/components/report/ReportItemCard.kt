package com.sos.chakhaeng.presentation.ui.components.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.report.ReportItem
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.errorLight
import com.sos.chakhaeng.presentation.theme.onSurfaceVariantLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.theme.surfaceLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportItemCard(
    reportItem: ReportItem,
    onClick: () -> Unit,
    onWatchVideo: (ReportItem) -> Unit = {},
    onDelete: (ReportItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // 메인 콘텐츠 영역1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 위반 유형 아이콘
                ViolationTypeIcon(
                    violationType = reportItem.violationType,
                    modifier = Modifier.size(40.dp)
                )

                // 메인 콘텐츠
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 위반 유형과 상태
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reportItem.violationType.displayName,
                            style = chakhaengTypography().bodyLarge,
                            fontWeight = FontWeight.Bold
                        )

                        StatusChip(status = reportItem.status)
                    }

                    // 번호판
                    Text(
                        text = reportItem.plateNumber,
                        style = chakhaengTypography().bodyMedium,
                    )
                }
            }

            // 메인 콘텐츠 영역2
            Column(
                modifier = modifier.padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ){
                // 위치
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = onSurfaceVariantLight
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = reportItem.location,
                        style = chakhaengTypography().bodyMedium,
                        color = onSurfaceVariantLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 시간
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = onSurfaceVariantLight
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = reportItem.occurredAt,
                        style = chakhaengTypography().bodyMedium,
                        color = onSurfaceVariantLight
                    )
                }

                // 동영상 파일
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = onSurfaceVariantLight
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = reportItem.videoFileName,
                        style = chakhaengTypography().bodyMedium,
                        color = onSurfaceVariantLight
                    )
                }
            }


            HorizontalDivider(
                thickness = 0.2.dp,
                color = onSurfaceVariantLight
            )

            // 하단 버튼 영역
            ReportItemButtons(
                reportItem = reportItem,
                onWatchVideo = onWatchVideo,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun ReportItemButtons(
    reportItem: ReportItem,
    onWatchVideo: (ReportItem) -> Unit,
    onDelete: (ReportItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.clickable{onWatchVideo(reportItem) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = primaryLight
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "영상보기",
                style = chakhaengTypography().bodyMedium,
                color = primaryLight
            )
        }

        Row(
            modifier = Modifier.clickable{onDelete(reportItem) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = errorLight
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "삭제",
                style = chakhaengTypography().bodyMedium,
                color = errorLight
            )
        }
    }
}