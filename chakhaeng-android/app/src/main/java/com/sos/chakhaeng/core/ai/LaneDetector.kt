package com.sos.chakhaeng.core.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp

class LaneDetector(
    private val interpreter: Interpreter,
    private val spec: LaneModelSpec
) {
    private var outputMap: MutableMap<Int, Any>? = null
    private var inputBuffer: ByteBuffer? = null

    init {
        ensureInputBuffer()
        ensureOutputBuffers()
    }

    /**
     * Bitmap → Lane 좌표
     */
    fun detect(bitmap: Bitmap): List<List<Pair<Int, Int>>> {
        val input = bitmapToInputBuffer(bitmap)
        val outputs = HashMap<Int, Any>()

        // [1, 100, 81, 4]
        val output0 = Array(1) { Array(100) { Array(81) { FloatArray(4) } } }
        // [1, 2, 81, 4]
        val output1 = Array(1) { Array(2) { Array(81) { FloatArray(4) } } }
        // [1, 200, 72, 4]
        val output2 = Array(1) { Array(200) { Array(72) { FloatArray(4) } } }
        // [1, 2, 72, 4]
        val output3 = Array(1) { Array(2) { Array(72) { FloatArray(4) } } }

        outputs[0] = output0
        outputs[1] = output1
        outputs[2] = output2
        outputs[3] = output3

        for (i in 0 until interpreter.outputTensorCount) {
            val tensor = interpreter.getOutputTensor(i)
            android.util.Log.d(
                "LaneDetector",
                "Output[$i] name=${tensor.name()} shape=${tensor.shape().contentToString()} dtype=${tensor.dataType()}"
            )
        }

        interpreter.runForMultipleInputsOutputs(arrayOf(input), outputs)
        return parseLanes(outputs, bitmap.width, bitmap.height)
    }

    /**
     * 시각화 (테스트용)
     */
    fun drawLanes(bitmap: Bitmap, lanes: List<List<Pair<Int, Int>>>): Bitmap {
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        // laneIdx별 색상
        val colors = arrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)

        for ((laneIdx, lane) in lanes.withIndex()) {
            val paint = Paint().apply {
                color = colors[laneIdx % colors.size]  // lane 0,1,2,3 → 다른 색
                strokeWidth = 4f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }

            for (i in 0 until lane.size - 1) {
                val (x1, y1) = lane[i]
                val (x2, y2) = lane[i + 1]
                canvas.drawLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), paint)
            }
        }

        return output
    }


    // ---------------- 내부 유틸 ----------------

    private fun ensureInputBuffer() {
        val cap = 1L * spec.preferInputH * spec.preferInputW * 3 * 4 // float32
        require(cap <= Int.MAX_VALUE)
        inputBuffer = ByteBuffer.allocateDirect(cap.toInt()).order(ByteOrder.nativeOrder())
    }

    private fun bitmapToInputBuffer(bitmap: Bitmap): ByteBuffer {
        // Python과 동일하게 full-height로 먼저 리사이즈
        val hFull = (spec.preferInputH / spec.cropRatio).toInt()   // 320 / 0.6 ≈ 533
        val resizedFull = Bitmap.createScaledBitmap(bitmap, spec.preferInputW, hFull, true)

        // crop offset (위쪽 잘라내기)
        val cutOffset = hFull - spec.preferInputH                  // 533 - 320 = 213
        val cropped = Bitmap.createBitmap(resizedFull, 0, cutOffset, spec.preferInputW, spec.preferInputH)

        // ByteBuffer 초기화
        val buf = inputBuffer ?: throw IllegalStateException("Input buffer not initialized")
        buf.clear()

        // 정규화 파라미터
        val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
        val std = floatArrayOf(0.229f, 0.224f, 0.225f)

        // BGR → RGB 변환 (Bitmap은 이미 ARGB_8888, Color.xxx로 접근하면 RGB 추출됨)
        for (y in 0 until spec.preferInputH) {
            for (x in 0 until spec.preferInputW) {
                val c = cropped.getPixel(x, y)
                val r = (Color.red(c) / 255.0f - mean[0]) / std[0]
                val g = (Color.green(c) / 255.0f - mean[1]) / std[1]
                val b = (Color.blue(c) / 255.0f - mean[2]) / std[2]
                buf.putFloat(r)
                buf.putFloat(g)
                buf.putFloat(b)
            }
        }

        buf.rewind()

        // 메모리 누수 방지
        if (resizedFull !== bitmap) resizedFull.recycle()
        if (cropped !== resizedFull && cropped !== bitmap) cropped.recycle()

        return buf
    }


    /**
     * 모델 출력 shape에 맞게 버퍼 생성
     */
    private fun ensureOutputBuffers() {
        outputMap = HashMap<Int, Any>().apply {
            this[0] = Array(1) { Array(spec.numCellRow) { Array(spec.numRow) { FloatArray(spec.numLanes) } } }
            this[1] = Array(1) { Array(spec.numCellCol) { Array(spec.numCol) { FloatArray(spec.numLanes) } } }
            this[2] = Array(1) { Array(2) { Array(spec.numRow) { FloatArray(spec.numLanes) } } }
            this[3] = Array(1) { Array(2) { Array(spec.numCol) { FloatArray(spec.numLanes) } } }
        }
    }

    /**
     * 후처리 (출력 좌표를 실제 Bitmap 크기로 scaling)
     */
    private fun parseLanes(outputs: Map<Int, Any>, outW: Int, outH: Int): List<List<Pair<Int, Int>>> {
        @Suppress("UNCHECKED_CAST")
        val locCol = outputs[0] as Array<Array<Array<FloatArray>>>
        @Suppress("UNCHECKED_CAST")
        val existCol = outputs[1] as Array<Array<Array<FloatArray>>>
        @Suppress("UNCHECKED_CAST")
        val locRow = outputs[2] as Array<Array<Array<FloatArray>>>
        @Suppress("UNCHECKED_CAST")
        val existRow = outputs[3] as Array<Array<Array<FloatArray>>>

        val numLanes = spec.numLanes
        val lanes = Array(numLanes) { mutableListOf<Pair<Int, Int>>() }

        val rowAnchor = FloatArray(spec.numRow) { i ->
            (1.0f - spec.cropRatio) + i.toFloat() / (spec.numRow - 1) * spec.cropRatio
        }
        val colAnchor = FloatArray(spec.numCol) { i ->
            i.toFloat() / (spec.numCol - 1)
        }

        fun softmax(arr: FloatArray): FloatArray {
            val maxVal = arr.maxOrNull() ?: 0f
            val expVals = arr.map { exp((it - maxVal).toDouble()).toFloat() }
            val sumExp = expVals.sum().takeIf { it > 0 } ?: 1f
            return expVals.map { it / sumExp }.toFloatArray()
        }

        // ROW 기반
        for (laneIdx in 0 until numLanes) {
            for (k in rowAnchor.indices) {
                val rowProb = softmax(
                    floatArrayOf(existRow[0][0][k][laneIdx], existRow[0][1][k][laneIdx])
                )
                if (rowProb[1] > 0.55f) {
                    val logits = FloatArray(spec.numCellRow) { c ->
                        locRow[0][c][k][laneIdx]
                    }
                    val sm = softmax(logits)

                    val maxIdx = sm.indices.maxByOrNull { sm[it] } ?: -1
                    if (maxIdx >= 0 && sm[maxIdx] > 0.5f) { // confidence 체크
                        val out = maxIdx + 0.5f
                        val xPx = (out / (spec.numCellRow - 1)) * outW
                        val yPx = rowAnchor[k] * outH
                        lanes[laneIdx].add(Pair(xPx.toInt(), yPx.toInt()))
                    }
                }
            }
        }

        // COL 기반
        for (laneIdx in 0 until numLanes) {
            for (k in colAnchor.indices) {
                val colProb = softmax(
                    floatArrayOf(existCol[0][0][k][laneIdx], existCol[0][1][k][laneIdx])
                )
                if (colProb[1] > 0.55f) {
                    val logits = FloatArray(spec.numCellCol) { c ->
                        locCol[0][c][k][laneIdx]
                    }
                    val sm = softmax(logits)

                    val maxIdx = sm.indices.maxByOrNull { sm[it] } ?: -1
                    if (maxIdx >= 0 && sm[maxIdx] > 0.5f) { // confidence 체크
                        val out = maxIdx + 0.5f
                        val yPx = (out / (spec.numCellCol - 1)) * outH
                        val xPx = colAnchor[k] * outW
                        lanes[laneIdx].add(Pair(xPx.toInt(), yPx.toInt()))
                    }
                }
            }
        }

        return lanes.filter { it.isNotEmpty() }
    }

}
