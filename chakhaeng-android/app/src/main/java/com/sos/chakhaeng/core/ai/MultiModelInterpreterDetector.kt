package com.sos.chakhaeng.core.ai

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.TimeoutCancellationException
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import java.util.ArrayDeque
import kotlin.math.abs
import kotlin.math.atan2

class MultiModelInterpreterDetector(
    private val context: Context,
    private val backend: Backend = Backend.CPU,
    specs: List<ModelSpec>,
    private val scope: CoroutineScope
) : Detector {

    // 🔒 YOLO 동기화
    private val mutex = Mutex()
    private val running = AtomicBoolean(false)

    // YOLO 캐시
    private var cachedOutArray: Array<Array<FloatArray>>? = null
    private var cachedOutShape: IntArray? = null
    private var cachedOutputsMap: HashMap<Int, Any>? = null
    private var cachedInputBuffer: ByteBuffer? = null
    private var cachedInputW = -1
    private var cachedInputH = -1
    private var cachedInputRange = InputRange.FLOAT32_0_1

    // Lane 관련
    private val laneDetector = LaneDetector(context, "models/culane_res18_dynamic.tflite")
    private val _laneFlow = MutableStateFlow<LaneDetection?>(null)
    val laneFlow: StateFlow<LaneDetection?> = _laneFlow.asStateFlow()

    @Volatile private var latestLaneFrame: Bitmap? = null

    // lane 안정화용 history
    private val laneHistories = mutableMapOf<Int, ArrayDeque<List<Pair<Float, Float>>>>()
    private val MAX_HISTORY = 5
    private val OUTLIER_THRESHOLD = 80f // 픽셀 단위 예시

    // Spec 관리
    private val specsByKey = specs.associateBy { it.key }.toMutableMap()
    private val labelCache = mutableMapOf<String, List<String>>()
    private var debugLoggedOnce = false
    private val interpreters = mutableMapOf<String, Interpreter>()
    private val parsers = mutableMapOf<String, YoloV8Parser>()
    private var currentKey: String = specs.first().key

    init {
        // LaneDetector 워커 루프
        scope.launch(Dispatchers.Default) {
            while (isActive) {
                try {
                    val frame = latestLaneFrame
                    if (frame != null && !frame.isRecycled) {
                        val safeBmp = try {
                            frame.copy(frame.config ?: Bitmap.Config.ARGB_8888, false)
                        } catch (t: Throwable) {
                            Log.w("LaneDetector", "bitmap copy failed", t)
                            null
                        }

                        if (safeBmp != null) {
                            try {
                                val coords = laneDetector.detect(safeBmp) // List<List<Pair<Float, Float>>>

                                if (coords.isNotEmpty()) {
                                    val stabilized = mutableListOf<List<Pair<Float, Float>>>()

                                    coords.forEachIndexed { idx, lane ->
                                        val history = laneHistories.getOrPut(idx) { ArrayDeque() }

                                        // ① 각도 계산
                                        val angle = computeSlopeDeg(lane)
                                        val isWeirdAngle = angle?.let { !isAnglePlausible(it) } ?: false

                                        // ② 중앙값 차선 구하기
                                        val medianLane = computeMedianLane(history)

                                        // ③ Outlier 판단
                                        val isOutlier = if (medianLane != null) {
                                            deviationTooLarge(lane, medianLane, OUTLIER_THRESHOLD) || isWeirdAngle
                                        } else isWeirdAngle

                                        if (!isOutlier) {
                                            if (history.size >= MAX_HISTORY) history.removeFirst()
                                            history.addLast(lane)
                                            stabilized.add(lane)
                                        } else {
                                            Log.d("LaneDetector", "lane $idx outlier ignored (angle=$angle), keep previous")
                                            stabilized.add(history.lastOrNull() ?: lane)
                                        }
                                    }

                                    _laneFlow.value = LaneDetection(stabilized)
                                } else {
                                    Log.d("LaneDetector", "empty coords, keep previous lanes")
                                }

                            } catch (e: Exception) {
                                Log.e("LaneDetector", "detect failed", e)
                            } finally {
                                if (!safeBmp.isRecycled) safeBmp.recycle()
                            }
                        }
                        latestLaneFrame = null
                    }
                } catch (t: Throwable) {
                    Log.e("LaneDetector", "worker loop error", t)
                }

                delay(33) // ~30fps
            }
        }
    }

    private fun computeSlopeDeg(lane: List<Pair<Float, Float>>): Float? {
        if (lane.size < 2) return null
        val (x1, y1) = lane.first()
        val (x2, y2) = lane.last()
        val angleRad = atan2(y2 - y1, x2 - x1)
        return Math.toDegrees(angleRad.toDouble()).toFloat()
    }

    private fun isAnglePlausible(angleDeg: Float): Boolean {
        // 보통 도로 차선은 수평에서 ±45° 범위를 넘지 않음
        return angleDeg in -45f..45f
    }

    /**
     * LaneDetector에 프레임 제출 (최신 프레임만 유지)
     */
    fun submitLaneFrame(bitmap: Bitmap) {
        latestLaneFrame?.recycle()
        val safeConfig = bitmap.config ?: Bitmap.Config.ARGB_8888
        latestLaneFrame = bitmap.copy(safeConfig, false)
    }

    fun switchModel(key: String) {
        require(specsByKey.containsKey(key)) { "Unknown model key: $key" }
        currentKey = key
    }

    // ---------------- Detector ----------------

    override suspend fun warmup() {
        val spec = requireNotNull(specsByKey[currentKey])
        val itp = ensureInterpreter(spec)

        val w = spec.resolvedInputW.takeIf { it > 0 } ?: spec.preferInputSize ?: 640
        val h = spec.resolvedInputH.takeIf { it > 0 } ?: spec.preferInputSize ?: 640

        ensureInputBuffer(w, h, spec.inputRange)
        val shape = itp.getOutputTensor(0).shape()
        ensureOutputBuffers(shape)

        Log.d("DTAG", "warmup prepared: in=(${w}x${h}), outShape=${shape.contentToString()}")
    }

    override suspend fun detect(bitmap: Bitmap, rotation: Int): List<Detection> {
        if (!running.compareAndSet(false, true)) return emptyList()
        try {
            val spec = requireNotNull(specsByKey[currentKey])
            val itp = interpreters.getOrPut(currentKey) { ensureInterpreter(spec) }
            val parser = parsers.getOrPut(currentKey) { YoloV8Parser(spec.numClasses) }
            val labels: List<String>? = labelsFor(spec)

            val inW = spec.resolvedInputW.takeIf { it > 0 } ?: spec.preferInputSize ?: 640
            val inH = spec.resolvedInputH.takeIf { it > 0 } ?: spec.preferInputSize ?: 640

            ensureInputBuffer(inW, inH, spec.inputRange)
            val input = bitmapToInputBufferInto(
                dst = cachedInputBuffer!!,
                src = bitmap, w = inW, h = inH,
                inputRange = spec.inputRange,
                colorOrder = spec.colorOrder
            )

            val outTensor = itp.getOutputTensor(0)
            val shape = outTensor.shape()
            if (!debugLoggedOnce) {
                val channels = shape.getOrNull(1) ?: -1
                val derivedClasses = if (channels >= 0) channels - 4 else -1
                Log.d("BTAG", "derivedClasses=$derivedClasses, labels=${labels?.size ?: 0}")
                debugLoggedOnce = true
            }

            ensureOutputBuffers(shape)

            val runTimeoutMs = when (backend) {
                Backend.GPU -> 5000L
                Backend.NNAPI -> 3000L
                Backend.CPU -> 2000L
            }

            val t0 = SystemClock.elapsedRealtime()
            try {
                withTimeout(runTimeoutMs) {
                    mutex.withLock {
                        itp.runForMultipleInputsOutputs(arrayOf(input), cachedOutputsMap!!)
                    }
                }
            } catch (t: TimeoutCancellationException) {
                Log.e("DTAG", "tflite.run timeout ($runTimeoutMs ms)-> skip this frame")
                return emptyList()
            }
            val t1 = SystemClock.elapsedRealtime()
            Log.d("DTAG", "tflite.run took=${t1 - t0}ms")

            val a = shape[1]
            val b = shape[2]
            val out = cachedOutArray!!

            return when {
                a == 4 + spec.numClasses -> {
                    parser.parseCHW(
                        out = out,
                        inputW = inW, inputH = inH,
                        origW = bitmap.width, origH = bitmap.height,
                        labels = labels
                    )
                }
                b == 4 + spec.numClasses -> {
                    parser.parseHWC(
                        out = out,
                        inputW = inW, inputH = inH,
                        origW = bitmap.width, origH = bitmap.height,
                        labels = labels
                    )
                }
                else -> error("Unsupported output shape: ${shape.contentToString()}")
            }
        } finally {
            running.set(false)
        }
    }

    private fun labelsFor(spec: ModelSpec): List<String> =
        labelCache.getOrPut(spec.key) {
            spec.labelMap ?: runCatching {
                FileUtil.loadLabels(context, "labels/${spec.key}.txt")
            }.getOrElse { emptyList() }
        }

    override fun close() {
        interpreters.values.forEach { runCatching { it.close() } }
        interpreters.clear()
        parsers.clear()
    }

    // ---------------- Internal ----------------

    private fun ensureInterpreter(spec: ModelSpec): Interpreter {
        interpreters[spec.key]?.let { return it }
        val model = FileUtil.loadMappedFile(context, spec.assetPath)
        val options = buildInterpreterOptions(backend)
        val itp = Interpreter(model, options)

        runCatching {
            val inT = itp.getInputTensor(0)
            val shape = inT.shape()
            spec.resolvedInputH = shape.getOrNull(1) ?: spec.preferInputSize ?: 0
            spec.resolvedInputW = shape.getOrNull(2) ?: spec.preferInputSize ?: 0
            spec.resolvedInputType = inT.dataType()
        }
        interpreters[spec.key] = itp
        return itp
    }

    private fun buildInterpreterOptions(backend: Backend): Interpreter.Options {
        return Interpreter.Options().apply {
            when (backend) {
                Backend.CPU -> {
                    setUseXNNPACK(true); setNumThreads(4)
                }
                Backend.NNAPI -> {
                    runCatching { addDelegate(NnApiDelegate()) }
                    setUseXNNPACK(false); setNumThreads(1)
                }
                Backend.GPU -> {
                    val compat = CompatibilityList()
                    if (compat.isDelegateSupportedOnThisDevice) {
                        val opts = compat.bestOptionsForThisDevice
                        val gpu = GpuDelegate(opts)
                        addDelegate(gpu)
                        setUseXNNPACK(false); setNumThreads(1)
                        Log.d("DTAG", "GPU delegate attached")
                    } else {
                        setUseXNNPACK(true); setNumThreads(4)
                        Log.w("DTAG", "GPU not supported -> fallback to CPU")
                    }
                }
            }
        }
    }

    private fun ensureInputBuffer(w: Int, h: Int, range: InputRange) {
        if (cachedInputBuffer != null &&
            cachedInputW == w && cachedInputH == h && cachedInputRange == range) return
        val bytesPerPix = when (range) {
            InputRange.FLOAT32_0_1 -> 4
            InputRange.UINT8_0_255 -> 1
        }
        val cap = 1L * w * h * 3 * bytesPerPix
        require(cap <= Int.MAX_VALUE)
        cachedInputBuffer = ByteBuffer.allocateDirect(cap.toInt()).order(ByteOrder.nativeOrder())
        cachedInputW = w; cachedInputH = h; cachedInputRange = range
    }

    private fun ensureOutputBuffers(shape: IntArray) {
        if (cachedOutShape != null && cachedOutShape!!.contentEquals(shape)) return
        require(shape.size == 3 && shape[0] == 1)
        val rows = shape[1]
        val cols = shape[2]
        val arr = Array(1) { Array(rows) { FloatArray(cols) } }
        cachedOutArray = arr; cachedOutShape = shape.copyOf()
        val map = cachedOutputsMap ?: HashMap<Int, Any>(1)
        map.clear(); map[0] = arr
        cachedOutputsMap = map
    }

    private fun bitmapToInputBufferInto(
        dst: ByteBuffer,
        src: Bitmap,
        w: Int, h: Int,
        inputRange: InputRange,
        colorOrder: ColorOrder
    ): ByteBuffer {
        val resized = if (src.width != w || src.height != h)
            Bitmap.createScaledBitmap(src, w, h, true) else src
        dst.clear()
        val pixels = IntArray(w * h)
        resized.getPixels(pixels, 0, w, 0, 0, w, h)
        var i = 0
        when (inputRange) {
            InputRange.FLOAT32_0_1 -> {
                for (y in 0 until h) for (x in 0 until w) {
                    val c = pixels[i++]
                    val r = ((c ushr 16) and 0xFF) / 255f
                    val g = ((c ushr 8) and 0xFF) / 255f
                    val b = (c and 0xFF) / 255f
                    when (colorOrder) {
                        ColorOrder.RGB -> { dst.putFloat(r); dst.putFloat(g); dst.putFloat(b) }
                        ColorOrder.BGR -> { dst.putFloat(b); dst.putFloat(g); dst.putFloat(r) }
                        else -> { dst.putFloat(r); dst.putFloat(g); dst.putFloat(b) }
                    }
                }
            }
            InputRange.UINT8_0_255 -> {
                for (y in 0 until h) for (x in 0 until w) {
                    val c = pixels[i++]
                    val r = ((c ushr 16) and 0xFF).toByte()
                    val g = ((c ushr 8) and 0xFF).toByte()
                    val b = (c and 0xFF).toByte()
                    when (colorOrder) {
                        ColorOrder.RGB -> { dst.put(r); dst.put(g); dst.put(b) }
                        ColorOrder.BGR -> { dst.put(b); dst.put(g); dst.put(r) }
                        else -> { dst.put(r); dst.put(g); dst.put(b) }
                    }
                }
            }
        }
        dst.rewind()
        if (resized !== src) resized.recycle()
        return dst
    }

    // ---------------- Lane 안정화 유틸 ----------------
    private fun computeMedianLane(history: ArrayDeque<List<Pair<Float, Float>>>): List<Pair<Float, Float>>? {
        if (history.isEmpty()) return null
        // 가장 최근 N개의 lane 중 기울기 기준 중앙값에 가까운 lane 선택
        val sorted = history.sortedBy { computeSlopeDeg(it) ?: 0f }
        return sorted[sorted.size / 2]
    }

    private fun deviationTooLarge(
        current: List<Pair<Float, Float>>,
        ref: List<Pair<Float, Float>>,
        threshold: Float
    ): Boolean {
        if (current.size != ref.size) return false
        val diffs = current.zip(ref).map { (c, r) ->
            abs(c.first - r.first) + abs(c.second - r.second)
        }
        return diffs.average() > threshold
    }
}
