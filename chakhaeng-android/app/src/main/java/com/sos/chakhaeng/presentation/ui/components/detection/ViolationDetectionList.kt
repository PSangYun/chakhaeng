package com.sos.chakhaeng.presentation.ui.components.detection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.core.utils.TimeAgo
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.violation.ViolationInRangeEntity
import com.sos.chakhaeng.presentation.theme.chakhaengTypography

private const val TAG = "ViolationDetectionList"

@Composable
fun ViolationDetectionList(
    violations: List<ViolationInRangeEntity>,
    onViolationClick: (ViolationInRangeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (violations.isEmpty()) {
        EmptyViolationList(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 120.dp
            )
        ) {
            items(violations) { violation ->
                ViolationDetectionItem(
                    violation = violation,
                    onClick = { onViolationClick(violation) },
                )
            }
        }
    }
}

@Composable
private fun ViolationDetectionItem(
    violation: ViolationInRangeEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ViolationThumbnail(
                violation = violation,
                modifier = Modifier.size(width = 108.dp, height = 81.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = violation.violationType.displayName,
                            style = chakhaengTypography().bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        // 신뢰도
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (violation.violationType) {
                                ViolationType.WRONG_WAY -> Color(0xFFFEF2F2)
                                ViolationType.SIGNAL -> Color(0xFFFEF2F2)
                                ViolationType.LANE -> Color(0xFFFFF7ED)
                                ViolationType.LOVE_BUG -> Color(0xFFFFF7ED)
                                ViolationType.NO_PLATE -> Color(0xFFFFF7ED)
                                ViolationType.NO_HELMET -> Color(0xFFFEFCE8)
                                ViolationType.OTHERS -> Color(0xFFFEFCE8)
                                else -> Color(0xFFFEFCE8)
                            },
                        ) {
                            Text(
                                text = when (violation.violationType) {
                                    ViolationType.WRONG_WAY -> "위험"
                                    ViolationType.SIGNAL -> "위험"
                                    ViolationType.LANE -> "주의"
                                    ViolationType.LOVE_BUG -> "주의"
                                    ViolationType.NO_PLATE -> "주의"
                                    ViolationType.NO_HELMET -> "경미"
                                    ViolationType.OTHERS -> "경미"
                                    else -> "경미"
                                },
                                style = chakhaengTypography().labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = when (violation.violationType) {
                                    ViolationType.WRONG_WAY -> Color(0xFFEF4444)
                                    ViolationType.SIGNAL -> Color(0xFFEF4444)
                                    ViolationType.LANE -> Color(0xFFF97316)
                                    ViolationType.LOVE_BUG -> Color(0xFFF97316)
                                    ViolationType.NO_PLATE -> Color(0xFFF97316)
                                    ViolationType.NO_HELMET -> Color(0xFFCA8A04)
                                    ViolationType.OTHERS -> Color(0xFFCA8A04)
                                    else -> Color(0xFFCA8A04)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }


                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = violation.plate ?: "번호판 감지 실패",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )


                }

                Spacer(modifier = Modifier.height(2.dp))

                // 위치 정보
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = violation.locationText ?: "구미시 진평동 543-2",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = TimeAgo.from(violation.occurredAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyViolationList(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "탐지 대기중",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "AI가 위반 사항을 탐지하면 여기에 표시됩니다",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}