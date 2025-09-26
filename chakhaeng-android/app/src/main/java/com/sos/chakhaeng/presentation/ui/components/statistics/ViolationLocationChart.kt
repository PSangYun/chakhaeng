package com.sos.chakhaeng.presentation.ui.components.statistics

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.sos.chakhaeng.core.session.GoogleAuthManager
import com.sos.chakhaeng.presentation.theme.ChakHaengTheme
import com.sos.chakhaeng.presentation.ui.screen.login.LoginScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.darkGreen
import com.sos.chakhaeng.presentation.theme.lightGreen
import com.sos.chakhaeng.presentation.theme.naverGreen
import com.sos.chakhaeng.presentation.theme.primaryLight

// 지역 통계 모델 (필요 시 domain 모듈로 이동)
data class RegionStatistic(
    val region: String,
    val count: Int,
    val percentage: Int // 0~100
)

enum class RegionSort { COUNT_DESC, REGION_ASC }

@Composable
fun RegionViolationSection(
    regionStats: List<RegionStatistic>,
    totalCount: Int = regionStats.size,
    modifier: Modifier = Modifier,
    sortBy: RegionSort = RegionSort.COUNT_DESC,
    topN: Int? = null
) {
    val sorted = when (sortBy) {
        RegionSort.COUNT_DESC -> regionStats.sortedByDescending { it.count }
        RegionSort.REGION_ASC -> regionStats.sortedBy { it.region }
    }
    val data = if (topN != null) sorted.take(topN) else sorted
    val maxCount = (data.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "구미시 지역별 위반 건 수",
                style = chakhaengTypography().titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 항목 리스트
            data.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 지역명
                    Text(
                        text = item.region,
                        style = chakhaengTypography().bodyMedium,
                        modifier = Modifier.widthIn(min = 56.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // 진행바 (최대값 대비 비율)
                    Row(
                        modifier = Modifier.width(180.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .fillMaxWidth()
                                .background(Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(item.count.toFloat() / maxCount)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(primaryLight)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column() {
                        Text(
                            text = "${item.count}건",
                            style = chakhaengTypography().bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // 퍼센트
                        Text(
                            text = "${item.percentage}%",
                            style = chakhaengTypography().bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            // 하단 안내
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(lightGreen)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = naverGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "지역 편중을 확인하고 집중 단속/캠페인을 계획해보세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = darkGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ViolationLocationPreview() {
    ChakHaengTheme {
        val sampleData = listOf(
            RegionStatistic("강남구", 120, 24),
            RegionStatistic("송파구", 95, 19),
            RegionStatistic("서초구", 80, 16),
            RegionStatistic("마포구", 60, 12),
            RegionStatistic("은평구", 45, 9),
            RegionStatistic("관악구", 40, 8),
            RegionStatistic("기타", 60, 12),
        )
        RegionViolationSection(
            regionStats = sampleData,
            totalCount = 500,
            modifier = Modifier.padding(16.dp)
        )
    }
}