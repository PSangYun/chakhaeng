package com.sos.chakhaeng.core.ai

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

class MultiModelInterpreterDetector(
    private val context: Context,
    private val backend: Backend = Backend.CPU,
    specs: List<ModelSpec>
) : Detector {

    // 🔒 JNI 크래시 방지용
    private val mutex = Mutex()
    private val running = AtomicBoolean(false)

    private var cachedOutArray: Array<Array<FloatArray>>? = null
    private var cachedOutShape: IntArray? = null
    private var cachedOutputsMap: HashMap<Int, Any>? = null

    private var cachedInputBuffer: ByteBuffer? = null
    private var cachedInputW = -1
    private var cachedInputH = -1

    private var cachedInputRange = InputRange.FLOAT32_0_1

    private val specsByKey = specs.associateBy { it.key }.toMutableMap()
    private val labelCache = mutableMapOf<String, List<String>>()

    private var debugLoggedOnce = false

    // key → lazy singletons
    private val interpreters = mutableMapOf<String, Interpreter>()
    private val parsers = mutableMapOf<String, YoloV8Parser>()

    private var currentKey: String = specs.first().key

    fun switchModel(key: String) {
        require(specsByKey.containsKey(key)) { "Unknown model key: $key" }
        currentKey = key
        // 필요 시 이 타이밍에 ensureInterpreter()로 선 로딩도 가능
    }

    // ---------------- Detector ----------------

    override suspend fun warmup() {
        val spec = requireNotNull(specsByKey[currentKey])
        val itp = ensureInterpreter(spec)

        val w = spec.resolvedInputW.takeIf { it > 0 } ?: spec.preferInputSize ?: 640
        val h = spec.resolvedInputH.takeIf { it > 0 } ?: spec.preferInputSize ?: 640

        // 입력 버퍼 미리 확보
        ensureInputBuffer(w, h, spec.inputRange)

        // 출력 버퍼 미리 확보
        val shape = itp.getOutputTensor(0).shape() // e.g. [1,84,8400]
        ensureOutputBuffers(shape)

        // mutex.withLock {
        //     itp.runForMultipleInputsOutputs(arrayOf(cachedInputBuffer!!), cachedOutputsMap!!)
        // }
        Log.d("DTAG", "warmup prepared: in=(${w}x${h}), outShape=${shape.contentToString()}")
    }

    override suspend fun detect(bitmap: Bitmap, rotation: Int): Pair<List<Detection>, LaneDetection> {
        Log.d("DTAG","detect entry")
        // 이미 처리 중이면 "비우고 최신만" 정책 – 바로 리턴
        if (!running.compareAndSet(false, true)) return Pair(emptyList(), LaneDetection(emptyList()))
        try {

            val laneDetector = LaneDetector(context, "models/culane_res18_dynamic.tflite")
            val coords = laneDetector.detect(bitmap)

            coords.forEachIndexed { i, lane ->
                Log.d("Lane", "lane#$i size=${lane.size}, sample=${lane.take(10)}")
            }
            Log.d("Lane", "detect() coords=${coords.size}, sample=${coords.firstOrNull()}")
            val laneResult = LaneDetection(coords)

            val spec = requireNotNull(specsByKey[currentKey])
            val itp = interpreters.getOrPut(currentKey) { ensureInterpreter(spec) }
            val parser = parsers.getOrPut(currentKey) { YoloV8Parser(spec.numClasses) }
            val labels: List<String>? = labelsFor(spec)

            val inW = spec.resolvedInputW.takeIf { it > 0 } ?: spec.preferInputSize ?: 640
            val inH = spec.resolvedInputH.takeIf { it > 0 } ?: spec.preferInputSize ?: 640
            val inType = spec.resolvedInputType

            ensureInputBuffer(inW, inH, spec.inputRange)
            val input = bitmapToInputBufferInto(
                dst = cachedInputBuffer!!,
                src = bitmap, w = inW, h = inH,
                inputRange = spec.inputRange,
                colorOrder = spec.colorOrder
            )

            Log.d("DTAG", "detect() in model=$currentKey")


            val outTensor = itp.getOutputTensor(0)
            val shape = outTensor.shape()
            Log.d("DTAG", "outShape=${shape.contentToString()}")
            if (!debugLoggedOnce) {
                Log.d("BTAG", "spec.numClasses=${spec.numClasses}")
                // 보통 YOLOv8: shape[1]이 4+numClasses (CHW), shape[2]가 N
                val channels = shape.getOrNull(1) ?: -1
                val derivedClasses = if (channels >= 0) channels - 4 else -1
                Log.d("BTAG", "derivedClasses=$derivedClasses, labels=${labels?.size ?: 0}")
                debugLoggedOnce = true
            }

            ensureOutputBuffers(shape)

            val runTimeoutMs = when (backend) {
                Backend.GPU -> 5000L   // ✅ GPU: 3~5초 권장 (첫 프레임 대비)
                Backend.NNAPI -> 3000L
                Backend.CPU -> 2000L
            }

            val t0 = SystemClock.elapsedRealtime()
            try {
                // 🔒 + ⏱️ 1500ms 타임아웃 (에뮬레이터면 2000ms까지도)
                withTimeout(runTimeoutMs) {
                    mutex.withLock {
                        itp.runForMultipleInputsOutputs(arrayOf(input), cachedOutputsMap!!)
                    }
                }
            } catch (t: TimeoutCancellationException) {
                Log.e("DTAG", "tflite.run timeout ($runTimeoutMs ms)-> skip this frame")
                return Pair(emptyList(), LaneDetection(emptyList()))
            }
            val t1 = SystemClock.elapsedRealtime()
            Log.d("DTAG", "tflite.run took=${t1 - t0}ms")

            val a = shape[1]
            val b = shape[2]
            val out = cachedOutArray!!

            Log.d("Lane_Final", "returning coords=${laneResult.coords.size}, sample=${laneResult.coords.firstOrNull()}")


            return when {
                a == 4 + spec.numClasses -> {
                    val detections = YoloV8Parser(spec.numClasses).parseCHW(
                        out = out,
                        inputW = inW, inputH = inH,
                        origW = bitmap.width, origH = bitmap.height,
                        labels = labels
                    )
                    Pair(detections, laneResult)
                }
                b == 4 + spec.numClasses -> {
                    val detections = YoloV8Parser(spec.numClasses).parseHWC(
                        out = out,
                        inputW = inW, inputH = inH,
                        origW = bitmap.width, origH = bitmap.height,
                        labels = labels
                    )
                    Pair(detections, laneResult)
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

    /** Interpreter를 만들고, 입력 메타(크기/타입)를 spec.resolved*에 주입 */
    private fun ensureInterpreter(spec: ModelSpec): Interpreter {
        interpreters[spec.key]?.let { return it }

        val model = FileUtil.loadMappedFile(context, spec.assetPath)
        val options = buildInterpreterOptions(backend)
        val itp = Interpreter(model, options)

        // 🔎 입력 메타 런타임 확인 → resolved 필드 채움
        runCatching {
            val inT = itp.getInputTensor(0)
            val shape = inT.shape() // 보통 [1, H, W, 3]
            spec.resolvedInputH = shape.getOrNull(1) ?: spec.preferInputSize ?: 0
            spec.resolvedInputW = shape.getOrNull(2) ?: spec.preferInputSize ?: 0
            spec.resolvedInputType = inT.dataType()
        }.onFailure {
            // 동적 shapeSignature가 필요한 모델이면 여기에 보강
        }

        interpreters[spec.key] = itp
        return itp
    }

    // 파일 내 아무 곳 (클래스 안 private 메서드로) 추가
    private fun buildInterpreterOptions(backend: Backend): Interpreter.Options {
        return Interpreter.Options().apply {
            when (backend) {
                Backend.CPU -> {
                    // CPU: XNNPACK 권장
                    setUseXNNPACK(true)
                    setNumThreads(4)
                }
                Backend.NNAPI -> {
                    // NNAPI: 기기별 편차가 커서 테스트 필요
                    runCatching { addDelegate(NnApiDelegate()) }
                    setUseXNNPACK(false)
                    setNumThreads(1)
                }
                Backend.GPU -> {
                    // ✅ GPU delegate (호환성 체크 후 부착, 미지원이면 CPU로 폴백)
                    val compat = CompatibilityList()
                    if (compat.isDelegateSupportedOnThisDevice) {
                        val opts = compat.bestOptionsForThisDevice
                        // 필요시 성능 옵션 조정 가능:
                        // opts.setPrecisionLossAllowed(true) // FP16 허용
                        // opts.setInferencePreference(GpuDelegate.Options.INFERENCE_PREFERENCE_SUSTAINED_SPEED)
                        val gpu = GpuDelegate(opts)
                        addDelegate(gpu)

                        // GPU 사용 시 XNNPACK/NNAPI는 끄는 편
                        setUseXNNPACK(false)
                        setNumThreads(1)
                        Log.d("DTAG", "GPU delegate attached")
                    } else {
                        // 폴백: CPU + XNNPACK
                        setUseXNNPACK(true)
                        setNumThreads(4)
                        Log.w("DTAG", "GPU not supported on this device -> fallback to CPU")
                    }
                }
            }
        }
    }



    /** 입력 버퍼 1회 할당 후 재사용 */
    private fun ensureInputBuffer(w: Int, h: Int, range: InputRange) {
        if (cachedInputBuffer != null &&
            cachedInputW == w && cachedInputH == h && cachedInputRange == range) return

        val bytesPerPix = when (range) {
            InputRange.FLOAT32_0_1 -> 4
            InputRange.UINT8_0_255 -> 1
        }
        val cap = 1L * w * h * 3 * bytesPerPix
        require(cap <= Int.MAX_VALUE) { "Input buffer too large: $cap" }

        cachedInputBuffer = ByteBuffer.allocateDirect(cap.toInt()).order(ByteOrder.nativeOrder())
        cachedInputW = w
        cachedInputH = h
        cachedInputRange = range
    }

    /** 출력 버퍼/맵 1회 할당 후 재사용 */
    private fun ensureOutputBuffers(shape: IntArray) {
        if (cachedOutShape != null && cachedOutShape!!.contentEquals(shape)) return

        require(shape.size == 3 && shape[0] == 1) { "Unexpected output: ${shape.contentToString()}" }
        val rows = shape[1] // 84
        val cols = shape[2] // N (e.g., 8400)

        // 1×rows×cols – Object array 재사용 (JNI 안전)
        val arr = Array(1) { Array(rows) { FloatArray(cols) } }
        cachedOutArray = arr
        cachedOutShape = shape.copyOf()

        // HashMap도 재사용 (put으로 교체)
        val map = cachedOutputsMap ?: HashMap<Int, Any>(1)
        map.clear()
        map[0] = arr
        cachedOutputsMap = map
    }

    /**
     * 기존 bitmapToInputBuffer를 대체: dst ByteBuffer를 비워 넣기
     */
    private fun bitmapToInputBufferInto(
        dst: ByteBuffer,
        src: Bitmap,
        w: Int, h: Int,
        inputRange: InputRange,
        colorOrder: ColorOrder // 네 프로젝트에 이미 있을 가능성이 큼. 없으면 enum class ColorOrder { RGB, BGR }
    ): ByteBuffer {
        val resized = if (src.width != w || src.height != h)
            Bitmap.createScaledBitmap(src, w, h, true)
        else src

        dst.clear()
        val pixels = IntArray(w * h)
        resized.getPixels(pixels, 0, w, 0, 0, w, h)
        var i = 0

        when (inputRange) {
            InputRange.FLOAT32_0_1 -> {
                // float32 [0,1]
                for (y in 0 until h) for (x in 0 until w) {
                    val c = pixels[i++]
                    val r = ((c ushr 16) and 0xFF) / 255f
                    val g = ((c ushr 8) and 0xFF) / 255f
                    val b = (c and 0xFF) / 255f
                    when (colorOrder) {
                        ColorOrder.RGB -> { dst.putFloat(r); dst.putFloat(g); dst.putFloat(b) }
                        ColorOrder.BGR -> { dst.putFloat(b); dst.putFloat(g); dst.putFloat(r) }
                        // 혹시 ColorOrder가 다른 값도 있으면 else로 RGB 처리
                        else -> { dst.putFloat(r); dst.putFloat(g); dst.putFloat(b) }
                    }
                }
            }
            InputRange.UINT8_0_255 -> {
                // uint8 [0,255]
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

    /** YOLOv8 계열 기본: [1, N, 5+C] */
    private fun makeOutputBuffer(spec: ModelSpec): Array<Array<FloatArray>> =
        Array(1) { Array(spec.maxDetections) { FloatArray(5 + spec.numClasses) } }
}
