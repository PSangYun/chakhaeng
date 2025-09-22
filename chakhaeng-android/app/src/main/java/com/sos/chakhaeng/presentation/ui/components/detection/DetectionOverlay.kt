package com.sos.chakhaeng.presentation.ui.components.detection

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.sos.chakhaeng.core.ai.Detection
import kotlin.math.max

@Composable
fun DetectionOverlay(
    detections: List<Detection>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        detections.forEach { d ->
            val left = d.box.left * w
            val top = d.box.top * h
            val right = d.box.right * w
            val bottom = d.box.bottom * h

            drawRect(
                color = Color(0xFF4CAF50),
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 3f)
            )

            drawIntoCanvas { canvas ->
                val p = android.graphics.Paint().apply {
                    textSize = 36f
                    color = android.graphics.Color.WHITE
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText(
                    "${d.label} ${(d.score * 100).toInt()}%",
                    left,
                    max(36f, top - 8f),
                    p
                )
            }
        }
    }
}