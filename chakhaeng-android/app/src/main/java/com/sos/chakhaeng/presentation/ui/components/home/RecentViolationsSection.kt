package com.sos.chakhaeng.presentation.ui.components.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.home.RecentViolation
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.R

@Composable
fun RecentViolationsSection(
    violations: List<RecentViolation>,
    onClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "실시간 위반 감지",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

        }

        Spacer(modifier = Modifier.height(12.dp))

        // 위반 내역이 없을 경우
        if (violations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "감지된 위반 내역이 없습니다",
                        style = chakhaengTypography().bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // 위반 내역 리스트
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                violations.forEach { violation ->
                    ViolationItem(
                        violation = violation,
                        onClick = onClick
                    )
                }
            }
        }
    }
}

@Composable
fun ViolationItem(
    violation: RecentViolation,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable{
                onClick(violation.violationId)
            }, // 고정 높이
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 위반 타입 아이콘
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = getSeverityColor(violation.type).copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(getViolationIcon(violation.type)),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 위반 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = violation.type,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Log.d("TAG", "ViolationItem: ${violation.type}")
                Text(
                    text = violation.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 시간 표시
            Column {
                Text(
                    text = violation.carNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatTimeAgo(violation.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// 유틸리티 함수들
private fun getSeverityColor(severity: String): Color {
    return when (severity) {
        "역주행" -> Color(0xFFF44336)
        "신호위반" -> Color(0xFFFF9800)
        "차선침법" -> Color(0xFFFFEB3B)
        "무번호판" -> Color(0xFFCDDC39)
        "헬멧 미착용" -> Color(0xFF9C27B0)
        "킥보드 2인이상" -> Color(0xFF03A9F4)
        else -> Color.Black
    }
}

private fun getViolationIcon(violationType: String):Int {
    return when (violationType) {
        "역주행" -> R.drawable.ic_wrong_way
        "신호위반" -> R.drawable.ic_traffic
        "차선침범" -> R.drawable.lane
        "무번호판" -> R.drawable.ic_plate
        "헬멧 미착용" -> R.drawable.ic_helmet
        "킥보드 2인이상" -> R.drawable.ic_scooter
        else -> R.drawable.ic_ete
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        minutes < 1 -> "방금 전"
        minutes < 60 -> "${minutes}분 전"
        hours < 24 -> "${hours}시간 전"
        days < 7 -> "${days}일 전"
        else -> "${days / 7}주 전"
    }
}