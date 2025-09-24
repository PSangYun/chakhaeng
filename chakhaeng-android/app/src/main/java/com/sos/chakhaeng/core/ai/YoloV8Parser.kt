package com.sos.chakhaeng.core.ai

import android.graphics.RectF
import org.tensorflow.lite.DataType
import kotlin.math.max

enum class Backend { CPU, NNAPI, GPU }

data class Normalization(
    val mean: Float = 0f,
    val std: Float = 255f
)

/** 카메라 → 전처리 → 인터프리터 → 후처리 전 과정을 설계 친화적으로 */
data class ModelSpec(
    // ---- 필수(사전 지식) ----
    val key: String,             // "signal", "helmet", "plate" 등
    val assetPath: String,       // "models/signal.tflite"
    val numClasses: Int,         // 라벨 수
    val maxDetections: Int,      // 출력 후보 수 (ex: 8400)
    val inputRange: InputRange = InputRange.FLOAT32_0_1,  // 모델이 기대하는 정규화 범위
    val colorOrder: ColorOrder = ColorOrder.RGB,          // 대부분 RGB
    val labelMap: List<String>? = null, // 라벨 파일 로딩 결과(선택)

    // ---- 선택(힌트) : 내보낼 때 알고 있는 입력 사이즈가 있다면 제공 ----
    val preferInputSize: Int? = null, // ex) 640. 동적모델/알 수 없으면 null

    // ---- 해결(런타임에서 인터프리터에게서 읽어 채움) ----
    var resolvedInputW: Int = 0,
    var resolvedInputH: Int = 0,
    var resolvedInputType: DataType = DataType.FLOAT32
)

enum class InputRange {
    FLOAT32_0_1,     // 보통의 YOLOv8 TFLite
    UINT8_0_255      // 양자화된 UINT8 모델
}

enum class ColorOrder { RGB, BGR }

class YoloV8Parser(
    private val numClasses: Int,
    private val scoreThreshold: Float = 0.30f,
    private val iouThreshold: Float = 0.45f,
    private val applySigmoid: Boolean = false
) {
    private fun sigm(x: Float) = (1f / (1f + kotlin.math.exp(-x)))

    // 좌표가 0~1 정규화인지 자동 판별
    private fun coordsAreNormalized(samples: List<Float>): Boolean {
        if (samples.isEmpty()) return false
        val maxAbs = samples.maxOf { kotlin.math.abs(it) }
        return maxAbs <= 1.05f   // 살짝 여유
    }

    /** out: [C,H,W] or [C,N] 계열 */
    fun parseCHW(
        out: Array<Array<FloatArray>>,
        inputW: Int, inputH: Int,
        origW: Int, origH: Int,
        labels: List<String>?
    ): List<Detection> {
        val ch = out[0]
        val count = ch[0].size // N

        // 샘플 몇 개로 판별
        val probe = buildList {
            for (i in 0 until minOf(count, 32)) {
                add(ch[0][i]); add(ch[1][i]); add(ch[2][i]); add(ch[3][i])
            }
        }
        val norm = coordsAreNormalized(probe)

        // ★ 여기만 바꿉니다
        val sx = if (norm) origW.toFloat() else origW.toFloat() / inputW
        val sy = if (norm) origH.toFloat() else origH.toFloat() / inputH

        val detections = ArrayList<Detection>(200)
        for (i in 0 until count) {
            val cx = ch[0][i]
            val cy = ch[1][i]
            val w  = ch[2][i]
            val h  = ch[3][i]

            var bestScore = Float.NEGATIVE_INFINITY
            var bestIdx = -1
            for (c in 0 until numClasses) {
                var s = ch[4 + c][i]
                if (applySigmoid) s = sigm(s)
                if (s > bestScore) { bestScore = s; bestIdx = c }
            }
            if (bestIdx < 0 || bestScore < scoreThreshold) continue

            val x1 = (cx - w/2f) * sx
            val y1 = (cy - h/2f) * sy
            val x2 = (cx + w/2f) * sx
            val y2 = (cy + h/2f) * sy

            detections += Detection(
                label = labels?.getOrNull(bestIdx) ?: bestIdx.toString(),
                score = bestScore,
                // 최종은 "원본 프레임 기준 정규화 LTRB"
                box = android.graphics.RectF(
                    (x1 / origW).coerceIn(0f,1f),
                    (y1 / origH).coerceIn(0f,1f),
                    (x2 / origW).coerceIn(0f,1f),
                    (y2 / origH).coerceIn(0f,1f)
                )
            )
        }
        return nms(detections, iouThreshold)
    }

    /** out: [1,N,84] */
    fun parseHWC(
        out: Array<Array<FloatArray>>,
        inputW: Int, inputH: Int,
        origW: Int, origH: Int,
        labels: List<String>?
    ): List<Detection> {
        val arr = out[0]
        val count = arr.size

        val probe = buildList {
            for (i in 0 until minOf(count, 32)) {
                val p = arr[i]
                add(p[0]); add(p[1]); add(p[2]); add(p[3])
            }
        }
        val norm = coordsAreNormalized(probe)

        // ★ 여기만 바꿉니다
        val sx = if (norm) origW.toFloat() else origW.toFloat() / inputW
        val sy = if (norm) origH.toFloat() else origH.toFloat() / inputH

        val detections = ArrayList<Detection>(200)
        for (i in 0 until count) {
            val p = arr[i]
            val cx = p[0]; val cy = p[1]; val w = p[2]; val h = p[3]

            var bestScore = Float.NEGATIVE_INFINITY
            var bestIdx = -1
            for (c in 0 until numClasses) {
                var s = p[4 + c]
                if (applySigmoid) s = sigm(s)
                if (s > bestScore) { bestScore = s; bestIdx = c }
            }
            if (bestIdx < 0 || bestScore < scoreThreshold) continue

            val x1 = (cx - w/2f) * sx
            val y1 = (cy - h/2f) * sy
            val x2 = (cx + w/2f) * sx
            val y2 = (cy + h/2f) * sy

            detections += Detection(
                label = labels?.getOrNull(bestIdx) ?: bestIdx.toString(),
                score = bestScore,
                box = android.graphics.RectF(
                    (x1 / origW).coerceIn(0f,1f),
                    (y1 / origH).coerceIn(0f,1f),
                    (x2 / origW).coerceIn(0f,1f),
                    (y2 / origH).coerceIn(0f,1f)
                )
            )
        }
        return nms(detections, iouThreshold)
    }

    private fun nms(dets: List<Detection>, iouTh: Float): List<Detection> {
        val sorted = dets.sortedByDescending { it.score }.toMutableList()
        val out = mutableListOf<Detection>()
        while (sorted.isNotEmpty()) {
            val a = sorted.removeAt(0)
            out += a
            val it = sorted.iterator()
            while (it.hasNext()) {
                val b = it.next()
                if (a.label == b.label && iou(a.box, b.box) > iouTh) it.remove()
            }
        }
        return out
    }

    // ⚠️ 여기 버그 수정: x2,y2는 min, x1,y1은 max
    private fun iou(a: RectF, b: RectF): Float {
        val x1 = max(a.left, b.left)
        val y1 = max(a.top, b.top)
        val x2 = kotlin.math.min(a.right, b.right)
        val y2 = kotlin.math.min(a.bottom, b.bottom)
        val interW = kotlin.math.max(0f, x2 - x1)
        val interH = kotlin.math.max(0f, y2 - y1)
        val inter = interW * interH
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)
        val union = areaA + areaB - inter + 1e-6f
        return inter / union
    }
}
