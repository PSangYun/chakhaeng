package com.sos.chakhaeng.presentation.ui.components.statistics.chart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.statistics.HourlyStatistic
import com.sos.chakhaeng.presentation.theme.NEUTRAL100
import com.sos.chakhaeng.presentation.theme.NEUTRAL400
import com.sos.chakhaeng.presentation.theme.RED300
import com.sos.chakhaeng.presentation.theme.primaryLight

@Composable
fun HourlyBarChart(
    data: List<HourlyStatistic>,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, delayMillis = 200),
        label = "curve_chart_animation"
    )

    // 데이터 기반 계산값
    val maxValue = remember(data) { data.maxOfOrNull { it.count } ?: 1 }
    val peakValue = remember(data) { data.maxOfOrNull { it.count } ?: 0 }

    // 색상들
    val chartColors = remember {
        ChartColors(
            primary = primaryLight,
            peak = RED300,
            text = NEUTRAL400,
            grid = NEUTRAL100
        )
    }

    // 그라데이션
    val areaGradient = remember(chartColors) {
        Brush.verticalGradient(
            colors = listOf(
                chartColors.primary.copy(alpha = 0.6f),
                chartColors.primary.copy(alpha = 0.05f)
            )
        )
    }

    // Paint 객체
    val paintObjects = remember(chartColors) {
        PaintObjects.create(chartColors)
    }

    // 시간 라벨
    val timeLabels = remember {
        listOf(0 to "0시", 6 to "6시", 12 to "12시", 18 to "18시")
    }

    LaunchedEffect(key1 = Unit) {
        animationPlayed = true
    }

    Column(modifier = modifier) {
        Text(
            text = "시간대별 위반 발생",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(horizontal = 16.dp)
        ) {
            drawOptimizedAreaChart(
                data = data,
                maxValue = maxValue,
                peakValue = peakValue,
                animatedProgress = animatedProgress,
                canvasSize = size,
                colors = chartColors,
                gradient = areaGradient,
                paints = paintObjects,
                timeLabels = timeLabels
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(primaryLight)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "위반 건 수가 많은 시간대에 주의하세요.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 색상 데이터 클래스
@Immutable
private data class ChartColors(
    val primary: Color,
    val peak: Color,
    val text: Color,
    val grid: Color
)

// Paint 객체들을 캐시하는 클래스
@Immutable
private data class PaintObjects(
    val label: android.graphics.Paint,
    val axis: android.graphics.Paint,
    val peak: android.graphics.Paint
) {
    companion object {
        fun create(colors: ChartColors): PaintObjects {
            return PaintObjects(
                label = android.graphics.Paint().apply {
                    color = colors.text.toArgb()
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                },
                axis = android.graphics.Paint().apply {
                    color = colors.text.toArgb()
                    textSize = 16f
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                },
                peak = android.graphics.Paint().apply {
                    color = colors.peak.toArgb()
                    textSize = 16f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    isFakeBoldText = true
                }
            )
        }
    }
}


// 차트 그리기 함수
private fun DrawScope.drawOptimizedAreaChart(
    data: List<HourlyStatistic>,
    maxValue: Int,
    peakValue: Int,
    animatedProgress: Float,
    canvasSize: Size,
    colors: ChartColors,
    gradient: Brush,
    paints: PaintObjects,
    timeLabels: List<Pair<Int, String>>
) {
    val chartWidth = canvasSize.width - 60.dp.toPx()
    val chartHeight = canvasSize.height - 70.dp.toPx()
    val chartLeft = 30.dp.toPx()
    val chartTop = 20.dp.toPx()
    val chartBottom = chartTop + chartHeight

    // 격자선
    for (i in 0..3) {
        val y = chartBottom - (chartHeight * i / 3)
        drawLine(
            color = colors.grid,
            start = Offset(chartLeft, y),
            end = Offset(chartLeft + chartWidth, y),
            strokeWidth = 1.dp.toPx()
        )
    }

    if (data.isEmpty()) return

    // 데이터 포인트 계산
    val stepX = chartWidth / (data.size - 1)
    val points = data.mapIndexed { index, stat ->
        val x = chartLeft + index * stepX
        val y = chartBottom - (stat.count.toFloat() / maxValue) * chartHeight * animatedProgress
        Offset(x, y)
    }

    if (points.size > 1) {
        // 경로 생성 - 단순한 직선 연결
        val areaPath = Path().apply {
            moveTo(points[0].x, chartBottom)
            lineTo(points[0].x, points[0].y)
            points.forEach { point -> lineTo(point.x, point.y) }
            lineTo(points.last().x, chartBottom)
            close()
        }

        // 영역 채우기
        drawPath(path = areaPath, brush = gradient)

        // 라인 그리기
        val linePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            points.drop(1).forEach { point -> lineTo(point.x, point.y) }
        }

        drawPath(
            path = linePath,
            color = colors.primary,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // 핵심 포인트만 그리기
        data.forEachIndexed { index, stat ->
            if (index % 4 == 0 || stat.count == peakValue) {
                val point = points[index]
                val isPeak = stat.count == peakValue && stat.count > 0
                val pointColor = if (isPeak) colors.peak else colors.primary
                val pointRadius = if (isPeak) 5.dp.toPx() else 3.dp.toPx()

                drawCircle(color = Color.White, radius = pointRadius + 1.dp.toPx(), center = point)
                drawCircle(color = pointColor, radius = pointRadius, center = point)

                if (isPeak && animatedProgress > 0.9f) {
                    drawContext.canvas.nativeCanvas.drawText(
                        "최고 건 수",
                        point.x,
                        point.y - 20.dp.toPx(),
                        paints.peak
                    )
                }
            }
        }
    }

    // X축 라벨
    timeLabels.forEach { (hour, label) ->
        if (hour < data.size) {
            val x = chartLeft + hour * stepX
            drawContext.canvas.nativeCanvas.drawText(label, x, chartBottom + 30.dp.toPx(), paints.label)
        }
    }

    // Y축 라벨
    for (i in 1..3) {
        val y = chartBottom - (chartHeight * i / 3)
        val value = (maxValue * i / 3)
        drawContext.canvas.nativeCanvas.drawText(
            value.toString(),
            chartLeft - 20.dp.toPx(),
            y + 6.dp.toPx(),
            paints.axis
        )
    }
}