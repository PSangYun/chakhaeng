package com.sos.chakhaeng.presentation.ui.components.statistics.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.statistics.MonthlyTrend
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.darkGreen
import com.sos.chakhaeng.presentation.theme.lightGreen
import com.sos.chakhaeng.presentation.theme.naverGreen
import com.sos.chakhaeng.presentation.theme.primaryLight

@Composable
fun MonthlyTrendSection(
    monthlyTrend: List<MonthlyTrend>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "월별 위반 건 수",
                style = chakhaengTypography().titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val maxCount = monthlyTrend.maxOfOrNull { it.count } ?: 1

            monthlyTrend.forEach { trend ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = trend.month,
                        style = chakhaengTypography().bodyMedium,
                    )

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 최대값 대비 비율을 나타내는 진행 바
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Gray.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(trend.count.toFloat() / maxCount)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(primaryLight)
                                )
                            }

                            Text(
                                text = "${trend.count}건",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        // 전월 대비 증감률
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {

                            Text(
                                text = if (trend.changeFromPreviousMonth > 0)
                                    "+${trend.changeFromPreviousMonth}%"
                                else if (trend.changeFromPreviousMonth < 0)
                                    "${trend.changeFromPreviousMonth}%"
                                else
                                    "변화없음",
                                style = MaterialTheme.typography.bodySmall,

                                color = if (trend.changeFromPreviousMonth > 0)
                                    Color.Red
                                else if (trend.changeFromPreviousMonth < 0)
                                    primaryLight
                                else
                                    Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                }
            }

            // 하단 안내 텍스트
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
                    tint = naverGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "월별 패턴을 확인하고 안전운전 계획을 세워보세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = darkGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}