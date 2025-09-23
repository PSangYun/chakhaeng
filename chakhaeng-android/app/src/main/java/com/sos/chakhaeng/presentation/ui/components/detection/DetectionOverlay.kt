package com.sos.chakhaeng.presentation.ui.components.detection

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    modifier: Modifier = Modifier,
    // YOLOv8 기본 입력크기(필요 시 모델 입력 크기에 맞춰 바꾸세요: 640/512 등)
    modelInputSize: Float = 640f
) {
    LaunchedEffect(detections) {
        // 원본 좌표 로그(스팸 방지로 상위 5개)
        detections.take(5).forEachIndexed { i, d ->
            android.util.Log.d("BBox",
                "raw[$i] l=${d.box.left}, t=${d.box.top}, r=${d.box.right}, b=${d.box.bottom}")
        }
    }

    Canvas(modifier) {
        val viewW = size.width
        val viewH = size.height

        detections.forEachIndexed { idx, d ->
            val maxRaw = maxOf(d.box.left, d.box.top, d.box.right, d.box.bottom)
            val isPixel = maxRaw > 1f

            // 1) 1차 스케일(정규화 가정)
            var l0 = if (isPixel) d.box.left else d.box.left * viewW
            var t0 = if (isPixel) d.box.top  else d.box.top  * viewH
            var r0 = if (isPixel) d.box.right else d.box.right * viewW
            var b0 = if (isPixel) d.box.bottom else d.box.bottom * viewH

            // 2) 🔥 폴백 보정: 정규화로 처리했는데도 너무 작다면(= 1/입력크기로 또 나뉜 흔적),
            //    YOLO 입력크기(modelInputSize) 만큼 다시 키워준다.
            val prelimW = kotlin.math.abs(r0 - l0)
            val prelimH = kotlin.math.abs(b0 - t0)
            val looksTiny = !isPixel && (prelimW < 5f || prelimH < 5f)

            if (looksTiny) {
                l0 *= modelInputSize
                r0 *= modelInputSize
                t0 *= modelInputSize
                b0 *= modelInputSize
                android.util.Log.d("BBox",
                    "fix[$idx] applied x$modelInputSize (w=${"%.1f".format(prelimW)}," +
                            " h=${"%.1f".format(prelimH)})")
            }

            // 3) 좌표 정렬/클램프
            val left   = minOf(l0, r0).coerceIn(0f, viewW)
            val right  = maxOf(l0, r0).coerceIn(0f, viewW)
            val top    = minOf(t0, b0).coerceIn(0f, viewH)
            val bottom = maxOf(t0, b0).coerceIn(0f, viewH)
            val boxW = right - left
            val boxH = bottom - top

            android.util.Log.d(
                "BBox",
                "calc[$idx] isPixel=$isPixel, view=(${viewW.toInt()}x${viewH.toInt()})," +
                        " px(l=${"%.1f".format(left)}, t=${"%.1f".format(top)}, r=${"%.1f".format(right)}, b=${"%.1f".format(bottom)})," +
                        " w=${"%.1f".format(boxW)}, h=${"%.1f".format(boxH)}"
            )

            if (boxW < 1f || boxH < 1f) {
                // 너무 작으면 점으로라도 표시
                drawCircle(Color(0xFFFF9800), radius = 6f, center = Offset(left, top))
                android.util.Log.d("BBox", "skip[$idx] too small -> dot (${left.toInt()}, ${top.toInt()})")
            } else {
                // ✅ 박스
                drawRect(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(left, top),
                    size = Size(boxW, boxH),
                    style = Stroke(width = 3f)
                )
                // 라벨
                val textY = if (top < 30f) (top + 30f) else (top - 8f)
                drawIntoCanvas { c ->
                    val p = android.graphics.Paint().apply {
                        textSize = 36f
                        color = android.graphics.Color.WHITE
                        isAntiAlias = true
                        setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
                    }
                    c.nativeCanvas.drawText("${d.label} ${(d.score * 100).toInt()}%", left, textY, p)
                }
            }
        }
    }
}
