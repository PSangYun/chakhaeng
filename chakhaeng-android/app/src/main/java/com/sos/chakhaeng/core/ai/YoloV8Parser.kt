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
    private val scoreThreshold: Float = 0.35f,
    private val iouThreshold: Float = 0.55f,
    private val applySigmoid: Boolean = false // 로그릿일 경우 true로 켜세요
) {
    private fun sigm(x: Float) = (1f / (1f + kotlin.math.exp(-x)))
    fun parseCHW(
        out: Array<Array<FloatArray>>,
        inputW: Int, inputH: Int,
        origW: Int, origH: Int,
        labels: List<String>?
    ): List<Detection> {
        val ch = out[0]                 // [84][8400]
        val count = ch[0].size          // 8400
        val res = ArrayList<Detection>(128)

        // 점수 처리 옵션 (필요 시)
        val useSigmoid = applySigmoid   // 기존 플래그 그대로 사용

        for (i in 0 until count) {
            val cx = ch[0][i]
            val cy = ch[1][i]
            val w  = ch[2][i]
            val h  = ch[3][i]

            // class score (YOLOv8 detect head는 obj가 따로 없음)
            var bestIdx = -1
            var bestScore = Float.NEGATIVE_INFINITY
            var k = 4
            while (k < 4 + numClasses) {
                var s = ch[k][i]
                if (useSigmoid) s = sigm(s)
                if (s > bestScore) { bestScore = s; bestIdx = k - 4 }
                k++
            }
            if (bestIdx < 0 || bestScore < scoreThreshold) continue

            // ✅ 파이썬 decode_yolo와 동일: (0..1) → 픽셀 → 최종 0..1(원본)
            val x1_px = (cx - w/2f) * origW
            val y1_px = (cy - h/2f) * origH
            val x2_px = (cx + w/2f) * origW
            val y2_px = (cy + h/2f) * origH

            // 화면 밖 클램프
            val x1 = x1_px.coerceIn(0f, origW.toFloat())
            val y1 = y1_px.coerceIn(0f, origH.toFloat())
            val x2 = x2_px.coerceIn(0f, origW.toFloat())
            val y2 = y2_px.coerceIn(0f, origH.toFloat())

            // 최종은 0..1 정규화(오버레이 전제)
            val left   = (x1 / origW).coerceIn(0f, 1f)
            val top    = (y1 / origH).coerceIn(0f, 1f)
            val right  = (x2 / origW).coerceIn(0f, 1f)
            val bottom = (y2 / origH).coerceIn(0f, 1f)

            res += Detection(
                label = labels?.getOrNull(bestIdx) ?: bestIdx.toString(),
                score = bestScore,
                box = RectF(left, top, right, bottom)
            )
        }
        return nmsWithLog(res, iouThreshold, classAgnostic = true)
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
        val res = ArrayList<Detection>(128)

        val useSigmoid = applySigmoid

        val letterbox = false

        val scale = if (letterbox) minOf(origW.toFloat() / inputW, origH.toFloat() / inputH) else 1f
        val padX  = if (letterbox) (origW - inputW * scale) / 2f else 0f
        val padY  = if (letterbox) (origH - inputH * scale) / 2f else 0f

        for (i in 0 until count) {
            val p = arr[i]
            val cx = p[0]   // 보통 0..1
            val cy = p[1]
            val w  = p[2]
            val h  = p[3]

            var bestIdx = -1
            var bestScore = Float.NEGATIVE_INFINITY
            var k = 4
            while (k < 4 + numClasses) {
                var s = p[k]
                if (useSigmoid) s = 1f / (1f + kotlin.math.exp(-s))
                if (s > bestScore) { bestScore = s; bestIdx = k - 4 }
                k++
            }
            if (bestIdx < 0 || bestScore < scoreThreshold) continue

            var x1 = (cx - w / 2f) * origW
            var y1 = (cy - h / 2f) * origH
            var x2 = (cx + w / 2f) * origW
            var y2 = (cy + h / 2f) * origH

            // 1-1) letterbox 보정
            if (letterbox) {
                val x1i = (cx - w / 2f) * inputW
                val y1i = (cy - h / 2f) * inputH
                val x2i = (cx + w / 2f) * inputW
                val y2i = (cy + h / 2f) * inputH
                x1 = ((x1i * scale) + padX)
                y1 = ((y1i * scale) + padY)
                x2 = ((x2i * scale) + padX)
                y2 = ((y2i * scale) + padY)
            }

            // 2) 클램프
            x1 = x1.coerceIn(0f, origW.toFloat())
            y1 = y1.coerceIn(0f, origH.toFloat())
            x2 = x2.coerceIn(0f, origW.toFloat())
            y2 = y2.coerceIn(0f, origH.toFloat())

            // 3) 최종은 0..1 정규화(오버레이 전제)
            val left   = (x1 / origW).coerceIn(0f, 1f)
            val top    = (y1 / origH).coerceIn(0f, 1f)
            val right  = (x2 / origW).coerceIn(0f, 1f)
            val bottom = (y2 / origH).coerceIn(0f, 1f)

            res += Detection(
                label = labels?.getOrNull(bestIdx) ?: bestIdx.toString(),
                score = bestScore,
                box = android.graphics.RectF(left, top, right, bottom)
            )
        }

        return nmsWithLog(res, iouThreshold, classAgnostic = true)
//        return nms(res, iouThreshold)
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

    private fun iou01(a: RectF, b: RectF): Float {
        // a,b는 0..1 정규화 박스 전제
        val xA = maxOf(a.left, b.left)
        val yA = maxOf(a.top, b.top)
        val xB = minOf(a.right, b.right)
        val yB = minOf(a.bottom, b.bottom)
        val w = (xB - xA).coerceAtLeast(0f)
        val h = (yB - yA).coerceAtLeast(0f)
        val inter = w * h
        if (inter <= 0f) return 0f
        val areaA = (a.right - a.left).coerceAtLeast(0f) * (a.bottom - a.top).coerceAtLeast(0f)
        val areaB = (b.right - b.left).coerceAtLeast(0f) * (b.bottom - b.top).coerceAtLeast(0f)
        if (areaA <= 0f || areaB <= 0f) return 0f
        return inter / (areaA + areaB - inter)
    }

    private fun nmsWithLog(
        dets: List<Detection>,
        iouThr: Float,
        classAgnostic: Boolean = true
    ): List<Detection> {
        if (dets.isEmpty()) return dets
        // 스코어 순으로 정렬
        val sorted = dets.sortedByDescending { it.score }.toMutableList()
        val kept = ArrayList<Detection>(sorted.size)
        var suppressed = 0

        while (sorted.isNotEmpty()) {
            val best = sorted.removeAt(0)
            kept += best

            val it = sorted.iterator()
            while (it.hasNext()) {
                val other = it.next()
                if (!classAgnostic && other.label != best.label) continue
                val iou = iou01(best.box, other.box)
                if (iou >= iouThr) {
                    // 🔎 겹침 로그 (상위 몇 개만)
                    if (kept.size <= 5) {
                        android.util.Log.d("NMS",
                            "suppress label=${other.label}, iou=${"%.2f".format(iou)} " +
                                    "score=${"%.2f".format(other.score)} by ${best.label}@${"%.2f".format(best.score)}")
                    }
                    suppressed++
                    it.remove()
                }
            }
        }
        android.util.Log.d(
            "NMS",
            "in=${dets.size}, kept=${kept.size}, suppressed=$suppressed, thr=$iouThr, classAgnostic=$classAgnostic"
        )
        return kept
    }

}
