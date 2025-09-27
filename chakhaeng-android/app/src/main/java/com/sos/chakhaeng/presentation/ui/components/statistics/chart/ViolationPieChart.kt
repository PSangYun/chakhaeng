package com.sos.chakhaeng.presentation.ui.components.statistics.chart

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.statistics.ViolationTypeStatistic
import com.sos.chakhaeng.presentation.theme.NEUTRAL100
import com.sos.chakhaeng.presentation.theme.NEUTRAL400
import com.sos.chakhaeng.presentation.theme.PieChartColors
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.lightGray

@Composable
fun ViolationPieChart(
    data: List<ViolationTypeStatistic>,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 2000),
        label = "pie_chart_animation"
    )

    val violationColorMap = remember {
        mapOf(
            "SIGNAL" to PieChartColors.SIGNAL,
            "LANE" to PieChartColors.LANE,
            "WRONG_WAY" to PieChartColors.WRONG_WAY,
            "NO_PLATE" to PieChartColors.NO_PLATE,
            "NO_HELMET" to PieChartColors.NO_HELMET,
            "NO_HELMET_AND_LANE" to PieChartColors.LANE,
            "NO_HELMET_AND_LOVE_BUG" to PieChartColors.WRONG_WAY,
            "OTHERS" to PieChartColors.OTHERS
        )
    }

    LaunchedEffect(key1 = Unit) {
        animationPlayed = true
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 차트 카드
        Card(
            modifier = Modifier
                .size(240.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.1f)
                ),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    drawCleanPieChart(
                        data = data,
                        violationColorMap = violationColorMap,
                        animatedProgress = animatedProgress,
                        center = center,
                        radius = size.minDimension / 2 * 0.85f
                    )
                }

                // 중앙 글래스모피즘 원
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = totalCount.toString(),
                            style = chakhaengTypography().headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                        )
                        Text(
                            text = "총 탐지",
                            style = chakhaengTypography().bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = NEUTRAL400
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 범례
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            data.forEachIndexed { index, item ->
                val color = violationColorMap[item.violationType.name]
                    ?: violationColorMap["OTHERS"]!!

                CleanLegendItem(
                    color = color,
                    label = item.violationType.displayName,
                    count = item.count,
                    percentage = item.percentage,
                    animatedProgress = animatedProgress
                )
            }
        }
    }
}

@Composable
private fun CleanLegendItem(
    color: Color,
    label: String,
    count: Int,
    percentage: Int,
    animatedProgress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 단일 색상 인디케이터
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                )

                Text(
                    text = label,
                    style = chakhaengTypography().bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 단일 색상 진행바
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(lightGray)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(percentage / 100f * animatedProgress)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }

                Text(
                    text = "${count}건",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "${percentage}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = NEUTRAL400
                )
            }
        }
    }
}

private fun DrawScope.drawCleanPieChart(
    data: List<ViolationTypeStatistic>,
    violationColorMap: Map<String, Color>,
    animatedProgress: Float,
    center: Offset,
    radius: Float
) {
    val total = data.sumOf { it.count }.toFloat()
    var startAngle = -90f
    val strokeWidth = 28.dp.toPx()
    val innerRadius = radius - strokeWidth

    // 배경 원 (연한 회색)
    drawCircle(
        color = Color.Transparent,
        radius = radius,
        center = center
    )

    data.forEachIndexed { index, item ->
        val sweepAngle = (item.count / total) * 360f * animatedProgress
        val color = violationColorMap[item.violationType.name]
            ?: violationColorMap["OTHERS"]!!

        // 메인 아크 (단일 색상)
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 강조 효과 (밝은 선)
        drawArc(
            color = Color.White.copy(alpha = 0.4f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(
                center.x - radius + strokeWidth * 0.15f,
                center.y - radius + strokeWidth * 0.15f
            ),
            size = Size(
                (radius - strokeWidth * 0.15f) * 2,
                (radius - strokeWidth * 0.15f) * 2
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        startAngle += sweepAngle
    }

    // 내부 그림자 효과
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.05f)
            ),
            radius = innerRadius * 0.8f
        ),
        radius = innerRadius,
        center = center
    )
}