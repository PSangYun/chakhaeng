package com.sos.chakhaeng.presentation.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

fun Modifier.angledLinearGradientBackground(
    colors: List<Color>,
    angleDeg: Float = 90f,
    colorStops: List<Pair<Float, Color>>? = null,
    shape: Shape = RectangleShape,
    tileMode: TileMode = TileMode.Clamp
): Modifier = this.then(
    Modifier.drawWithCache {
        // 컴포넌트 실제 크기에 맞춰 start/end 계산
        val w = size.width
        val h = size.height
        val rad = Math.toRadians(angleDeg.toDouble())
        val vx = cos(rad).toFloat()
        val vy = sin(rad).toFloat()
        val len = hypot(w, h)
        val cx = w / 2f
        val cy = h / 2f
        val start = Offset(cx - vx * len / 2f, cy - vy * len / 2f)
        val end   = Offset(cx + vx * len / 2f, cy + vy * len / 2f)

        val brush = if (colorStops != null)
            Brush.linearGradient(colorStops = colorStops.toTypedArray(), start, end, tileMode)
        else
            Brush.linearGradient(colors, start, end, tileMode)

        onDrawWithContent {
            val outline = shape.createOutline(size, layoutDirection, this)
            drawOutline(outline = outline, brush = brush, alpha = 1f)
            drawContent()

        }
    }
)