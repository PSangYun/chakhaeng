package com.sos.chakhaeng.core.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

private const val TAG = "LaneDetector"

class LaneDetector(
    context: Context,
    modelPath: String,
    private val cropRatio: Float = 0.6f,   // default = 0.6
    numThreads: Int = 4
) {
    private var interpreter: Interpreter
    private var inputShape: IntArray

    init {
        val modelBuffer = loadModelFile(context, modelPath)
        val options = Interpreter.Options().apply { setNumThreads(numThreads) }
        interpreter = Interpreter(modelBuffer, options)
        inputShape = interpreter.getInputTensor(0).shape()   // [1, 320, 1600, 3]
    }

    @Throws(IOException::class)
    private fun loadModelFile(context: Context, modelPath: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * CameraX에서 받은 Bitmap을 전처리 → ByteBuffer
     */
    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val inputW = inputShape[2] // 1600
        val inputH = inputShape[1] // 320
        val hFull = (inputH / cropRatio).toInt()  // 533
        val cutOffset = hFull - inputH            // 213

        val resizedFull = Bitmap.createScaledBitmap(bitmap, inputW, hFull, true)
        val cropped = Bitmap.createBitmap(resizedFull, 0, cutOffset, inputW, inputH)

        val byteBuffer = ByteBuffer.allocateDirect(4 * inputW * inputH * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
        val std = floatArrayOf(0.229f, 0.224f, 0.225f)

        val pixels = IntArray(inputW * inputH)
        cropped.getPixels(pixels, 0, inputW, 0, 0, inputW, inputH)

        var idx = 0
        for (y in 0 until inputH) {
            for (x in 0 until inputW) {
                val value = pixels[idx++]
                val r = ((value shr 16) and 0xFF) / 255.0f
                val g = ((value shr 8) and 0xFF) / 255.0f
                val b = (value and 0xFF) / 255.0f
                byteBuffer.putFloat((r - mean[0]) / std[0])
                byteBuffer.putFloat((g - mean[1]) / std[1])
                byteBuffer.putFloat((b - mean[2]) / std[2])
            }
        }
        return byteBuffer
    }

    /**
     * 추론 + 후처리까지 한번에
     */
    fun detect(
        bitmap: Bitmap,
        tauRow: Float = 0.40f,
        tauCol: Float = 0.40f,
        minPtsRow: Int = 6,
        minPtsCol: Int = 8,
        localWidth: Int = 10,
        rowLaneIdx: IntArray = intArrayOf(1), // 기본적으로 1,2번 레인만
        colLaneIdx: IntArray = intArrayOf(),
        sortLeftToRight: Boolean = true
    ): List<List<Pair<Float, Float>>> {
        Log.d(TAG, "bitmap size: ${bitmap.width}, ${bitmap.height}")
        val inputBuffer = preprocess(bitmap)

        // 출력 버퍼 준비
        val locCol = Array(1) { Array(100) { Array(81) { FloatArray(4) } } }
        val existCol = Array(1) { Array(2) { Array(81) { FloatArray(4) } } }
        val locRow = Array(1) { Array(200) { Array(72) { FloatArray(4) } } }
        val existRow = Array(1) { Array(2) { Array(72) { FloatArray(4) } } }

        val outputs = hashMapOf<Int, Any>(
            0 to locCol,
            1 to existCol,
            2 to locRow,
            3 to existRow
        )

        interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)

        // === 후처리 준비 ===
        val numRow = locRow[0][0].size
        val numCol = locCol[0][0].size
        val numLanes = locRow[0][0][0].size
        val gridRow = locRow[0].size
        val gridCol = locCol[0].size

        val inputW = inputShape[2] // 1600
        val inputH = inputShape[1] // 320
        val hFull = (inputH / cropRatio).toInt()  // 533
        val cutOffset = hFull - inputH            // 213

        fun softmax(x: FloatArray): FloatArray {
            val maxVal = x.maxOrNull() ?: 0f
            val expVals = x.map { exp((it - maxVal).toDouble()).toFloat() }
            val sum = expVals.sum()
            return expVals.map { it / sum }.toFloatArray()
        }

        val existRowProb = Array(numRow) { FloatArray(numLanes) }
        val existColProb = Array(numCol) { FloatArray(numLanes) }
        for (r in 0 until numRow) {
            for (l in 0 until numLanes) {
                val logits = FloatArray(2) { existRow[0][it][r][l] }
                val probs = softmax(logits)
                existRowProb[r][l] = probs[1]
            }
        }
        for (c in 0 until numCol) {
            for (l in 0 until numLanes) {
                val logits = FloatArray(2) { existCol[0][it][c][l] }
                val probs = softmax(logits)
                existColProb[c][l] = probs[1]
            }
        }

        val rowAnchor = FloatArray(numRow) { i ->
            (1.0f - cropRatio) + (i.toFloat() / (numRow - 1)) * cropRatio
        }
        val colAnchor = FloatArray(numCol) { i -> i.toFloat() / (numCol - 1) }
        val lanes = Array(numLanes) { mutableListOf<Pair<Float, Float>>() }

        // === ROW 기반 (y 고정, x 예측) ===
        for (i in rowLaneIdx) {
            val active = (0 until numRow).filter { r -> existRowProb[r][i] > tauRow }
            if (active.size < minPtsRow) continue

            val pts = mutableListOf<Pair<Float, Float>>()
            for (k in active) {
                val center = locRow[0].indices.maxByOrNull { idx -> locRow[0][idx][k][i] } ?: continue
                val L = max(0, center - localWidth)
                val R = min(gridRow - 1, center + localWidth)

                val sliceLogits = FloatArray(R - L + 1) { idx -> locRow[0][L + idx][k][i] }
                val probs = softmax(sliceLogits)
                val out = probs.mapIndexed { idx, p -> (L + idx) * p }.sum() + 0.5f

                val xFull = (out / (gridRow - 1)) * inputW
                val xNorm = xFull / inputW

                val yFull = rowAnchor[k] * hFull
                val yCrop = yFull - cutOffset

                if (yCrop in 0f..inputH.toFloat()) {
                    val yNorm = yFull / hFull
                    pts.add(Pair(xNorm, yNorm))
                }
            }
            if (pts.isNotEmpty()) {
                pts.sortBy { it.second }
                lanes[i].addAll(pts)
            }
        }

        // === COL 기반 (x 고정, y 예측) ===
        for (i in colLaneIdx) {
            val active = (0 until numCol).filter { c -> existColProb[c][i] > tauCol }
            if (active.size < minPtsCol) continue

            val pts = mutableListOf<Pair<Float, Float>>()
            for (k in active) {
                val center = locCol[0].indices.maxByOrNull { idx -> locCol[0][idx][k][i] } ?: continue
                val L = max(0, center - localWidth)
                val R = min(gridCol - 1, center + localWidth)

                val sliceLogits = FloatArray(R - L + 1) { idx -> locCol[0][L + idx][k][i] }
                val probs = softmax(sliceLogits)
                val out = probs.mapIndexed { idx, p -> (L + idx) * p }.sum() + 0.5f

                val yFull = (out / (gridCol - 1)) * hFull
                val yCrop = yFull - cutOffset

                if (yCrop in 0f..inputH.toFloat()) {
                    val yNorm = yFull / hFull
                    val xFull = colAnchor[k] * inputW
                    val xNorm = xFull / inputW
                    pts.add(Pair(xNorm, yNorm))
                }
            }
            if (pts.isNotEmpty()) {
                pts.sortBy { it.second }
                lanes[i].addAll(pts)
            }
        }

        // === RANSAC 직선 피팅 후 반환 ===
        val coords = lanes.filter { it.isNotEmpty() }.mapNotNull { pts ->
            val model = ransacLineFit(pts)
            if (model != null) {
                val (a, b) = model
                val yStart = 0.4f
                val yEnd = 1.0f
                val x1 = (yStart - b) / a
                val x2 = (yEnd - b) / a
                listOf(Pair(x1, yStart), Pair(x2, yEnd))
            } else null
        }

        return if (sortLeftToRight) {
            coords.sortedBy { pts ->
                val ys = pts.map { it.second }
                val yCut = ys.sorted()[(ys.size * 0.8).toInt().coerceAtMost(ys.size - 1)]
                val xsBottom = pts.filter { it.second >= yCut }.map { it.first }.ifEmpty { pts.map { it.first } }
                xsBottom.sorted()[xsBottom.size / 2]
            }
        } else coords
    }

    private fun ransacLineFit(
        points: List<Pair<Float, Float>>,
        maxTrials: Int = 20,
        threshold: Float = 0.02f, // 정규화 단위 허용 오차
        minInliers: Int = 6
    ): Pair<Float, Float>? {
        if (points.size < 2) return null

        var bestModel: Pair<Float, Float>? = null
        var bestInliers = 0

        repeat(maxTrials) {
            val sample = points.shuffled().take(2)
            val (x1, y1) = sample[0]
            val (x2, y2) = sample[1]
            if (x1 == x2) return@repeat

            val a = (y2 - y1) / (x2 - x1)
            val b = y1 - a * x1

            val inliers = points.count { (x, y) ->
                val yHat = a * x + b
                kotlin.math.abs(y - yHat) < threshold
            }

            if (inliers > bestInliers && inliers >= minInliers) {
                bestInliers = inliers
                bestModel = a to b
            }
        }

        return bestModel
    }
}
