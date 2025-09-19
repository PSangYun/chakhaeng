package com.sos.chakhaeng.presentation.ui.components.statistics.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.sos.chakhaeng.R
import com.sos.chakhaeng.domain.model.statistics.ReportStatistics
import com.sos.chakhaeng.presentation.ui.components.statistics.StatCard
import androidx.core.graphics.toColorInt
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.naverGreen

@Composable
fun ReportStatisticsSection(
    reportStats: ReportStatistics
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 신고 통계 카드들
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "총 신고 건수",
                value = reportStats.totalReports.toString(),
                iconVector = painterResource(R.drawable.ic_report_gd),
                iconColor = Color.Unspecified
            )

            StatCard(
                modifier = Modifier.weight(1f),
                title = "처리 완료",
                value = reportStats.completedReports.toString(),
                iconVector = painterResource(R.drawable.ic_success_gd),
                iconColor = Color.Unspecified
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "처리 중",
                value = reportStats.pendingReports.toString(),
                iconVector = painterResource(R.drawable.ic_processing_gd),
                iconColor = Color.Unspecified
            )

            StatCard(
                modifier = Modifier.weight(1f),
                title = "신고 성공률",
                value = "${reportStats.successRate}%",
                iconVector = painterResource(R.drawable.ic_target_gd),
                iconColor = Color.Unspecified
            )
        }

        // 신고 처리 현황
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "신고 처리 현황",
                    style = chakhaengTypography().titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                reportStats.reportStatusStats.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(status.color.toColorInt()))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = status.status,
                                style = chakhaengTypography().bodyMedium
                            )
                        }

                        Text(
                            text = "${status.count}건",
                            style = chakhaengTypography().bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 신고 성공률 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = naverGreen
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "신고 성공률",
                            style = chakhaengTypography().titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "${reportStats.successRate}%",
                            style = chakhaengTypography().headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "${reportStats.totalSuccessRate}%",
                            style = chakhaengTypography().bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}