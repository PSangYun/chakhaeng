package com.sos.chakhaeng.presentation.ui.components.detection

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.sos.chakhaeng.core.ai.TrackObj
import kotlin.text.toFloat
import kotlin.times

@Composable
fun TrackingOverlay(
    tracks: List<TrackObj>,
    coords: List<List<Pair<Float, Float>>> = emptyList(),
    modifier: Modifier = Modifier
) {
    Canvas(modifier)
    {
        val viewW = size.width
        val viewH = size.height

        tracks.forEach { t ->
            // TrackObj는 정규화 좌표라고 가정(x,y,w,h in 0..1)
            val left = t.box.x * viewW
            val top = t.box.y * viewH
            val right = (t.box.x + t.box.w) * viewW
            val bottom = (t.box.y + t.box.h) * viewH

            val idColor = colorForId(t.id)
            drawRect(
                color = idColor,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 3f)
            )

            drawIntoCanvas { c ->
                val p = android.graphics.Paint().apply {
                    textSize = 36f
                    color = android.graphics.Color.WHITE
                    isAntiAlias = true
                    setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
                }
                val label = "ID:${t.id} ${t.label}"
                val y = if (top < 30f) top + 30f else top - 8f
                c.nativeCanvas.drawText(label, left, y, p)
            }
        }
        coords.forEach { lane ->
            // lane은 List<Pair<Int, Int>>
            lane.zipWithNext { p1, p2 ->
                android.util.Log.d("Overlay", "lane point: x=${p1.first}, y=${p1.second}, " +
                        "scaled=(${p1.first * viewW}, ${p1.second * viewH}), view=(${viewW}x$viewH)")
                drawLine(
                    color = Color.Red,
                    start = Offset(p1.first.toFloat() * viewW, p1.second.toFloat() * viewH),
                    end   = Offset(p2.first.toFloat() * viewW, p2.second.toFloat() * viewH),
                    strokeWidth = 12f
                )
            }
        }
    }
}

// ID별로 고유 색
private fun colorForId(id: Int): Color {
    val hue = (id * 137.508f) % 360f    // golden angle
    val intColor = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.85f, 0.95f))
    return Color(intColor)
}