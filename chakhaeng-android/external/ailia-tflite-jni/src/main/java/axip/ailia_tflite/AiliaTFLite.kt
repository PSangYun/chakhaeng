package axip.ailia_tflite

import android.util.Log

class AiliaTFLite {
    companion object {
        const val AILIA_TFLITE_TENSOR_TYPE_FLOAT32 = 1
        const val AILIA_TFLITE_TENSOR_TYPE_FLOAT16 = 19
        const val AILIA_TFLITE_TENSOR_TYPE_INT32 = 2
        const val AILIA_TFLITE_TENSOR_TYPE_UINT8 = 3
        const val AILIA_TFLITE_TENSOR_TYPE_INT64 = 4
        const val AILIA_TFLITE_TENSOR_TYPE_STRING = 5
        const val AILIA_TFLITE_TENSOR_TYPE_BOOL = 6
        const val AILIA_TFLITE_TENSOR_TYPE_INT16 = 7
        const val AILIA_TFLITE_TENSOR_TYPE_COMPLEX64 = 8
        const val AILIA_TFLITE_TENSOR_TYPE_INT8 = 9

        const val AILIA_TFLITE_STATUS_SUCCESS = 0
        const val AILIA_TFLITE_STATUS_INVALID_ARGUMENT = -1
        const val AILIA_TFLITE_STATUS_OUT_OF_RANGE = -2
        const val AILIA_TFLITE_STATUS_MEMORY_INSUFFICIENT = -3
        const val AILIA_TFLITE_STATUS_BROKEN_MODEL = -4
        const val AILIA_TFLITE_STATUS_INVALID_PARAMETER = -5
        const val AILIA_TFLITE_STATUS_PARAMETER_NOT_FOUND = -6
        const val AILIA_TFLITE_STATUS_UNSUPPORTED_OPCODE = -7
        const val AILIA_TFLITE_STATUS_LICENSE_NOT_FOUND = -8
        const val AILIA_TFLITE_STATUS_LICENSE_BROKEN = -9
        const val AILIA_TFLITE_STATUS_LICENSE_EXPIRED = -10
        const val AILIA_TFLITE_STATUS_INVALID_STATE = -11
        const val AILIA_TFLITE_STATUS_OTHER_ERROR = -128

        const val AILIA_TFLITE_ENV_REFERENCE = 0
        const val AILIA_TFLITE_ENV_NNAPI = 1
        const val AILIA_TFLITE_ENV_MMALIB = 2
        const val AILIA_TFLITE_ENV_MMALIB_COMPATIBLE = 3
        const val AILIA_TFLITE_ENV_QNN = 4

        const val AILIA_TFLITE_MEMORY_MODE_DEFAULT = 0
        const val AILIA_TFLITE_MEMORY_MODE_REDUCE_INTERSTAGE = 1

        const val AILIA_TFLITE_PROFILE_MODE_DISABLE = 0
        const val AILIA_TFLITE_PROFILE_MODE_ENABLE = 1
        const val AILIA_TFLITE_PROFILE_MODE_TRACE = 2
        const val AILIA_TFLITE_PROFILE_MODE_MEMORY = 3

        const val AILIA_TFLITE_FLAG_NONE = 0
        const val AILIA_TFLITE_FLAG_INPUT_AND_OUTPUT_TENSORS_USE_SCRATCH = 1

        const val AILIA_TFLITE_CPU_FEATURES_NONE = 0x00000000
        const val AILIA_TFLITE_CPU_FEATURES_NEON = 0x00000001
        const val AILIA_TFLITE_CPU_FEATURES_SSE2 = 0x00000002
        const val AILIA_TFLITE_CPU_FEATURES_SSE4_2 = 0x00000004
        const val AILIA_TFLITE_CPU_FEATURES_AVX = 0x00000008
        const val AILIA_TFLITE_CPU_FEATURES_AVX2 = 0x00000010
        const val AILIA_TFLITE_CPU_FEATURES_VNNI = 0x00000020
        const val AILIA_TFLITE_CPU_FEATURES_AVX512 = 0x00000040
        const val AILIA_TFLITE_CPU_FEATURES_I8MM = 0x00000080

        init {
            System.loadLibrary("ailia_tflite")
        }
    }

    private val tag = AiliaTFLite::class.simpleName
    private var instance: Long = 0

    fun getEnvironments(): IntArray? {
        val count = getEnvironmentCount()
        if (count <= 0) return null
        return getEnvironment(count)
    }

    fun open(modelData: ByteArray, envId: Int = AILIA_TFLITE_ENV_REFERENCE,
               memoryMode: Int = AILIA_TFLITE_MEMORY_MODE_DEFAULT, 
               flags: Int = AILIA_TFLITE_FLAG_NONE): Boolean {
        instance = create(modelData, envId, memoryMode, flags)
        if (instance == 0L) {
            Log.e(tag, "Failed to create ailia TFLite instance")
            return false
        }
        return true
    }

    fun allocateTensors(): Boolean {
        val result = allocateTensors(instance)
        if (result != AILIA_TFLITE_STATUS_SUCCESS) {
            Log.e(tag, "Failed to allocate tensors: $result")
            return false
        }
        return true
    }

    fun resizeInputTensor(inputIndex: Int, shape: IntArray): Boolean {
        val result = resizeInputTensor(instance, inputIndex, shape, shape.size)
        if (result != AILIA_TFLITE_STATUS_SUCCESS) {
            Log.e(tag, "Failed to resize input tensor: $result")
            return false
        }
        return true
    }

    fun getNumberOfInputs(): Int {
        val result = getNumberOfInputs(instance)
        if (result < 0) {
            Log.e(tag, "Failed to get number of inputs")
            return 0
        }
        return result
    }

    fun getNumberOfOutputs(): Int {
        val result = getNumberOfOutputs(instance)
        if (result < 0) {
            Log.e(tag, "Failed to get number of outputs")
            return 0
        }
        return result
    }

    fun getInputTensorIndex(inputIndex: Int): Int {
        return getInputTensorIndex(instance, inputIndex);
    }

    fun getOutputTensorIndex(inputIndex: Int): Int {
        return getOutputTensorIndex(instance, inputIndex);
    }

    fun getInputTensorShape(inputIndex: Int): IntArray? {
        return getTensorShape(instance, getInputTensorIndex(instance, inputIndex))
    }

    fun getOutputTensorShape(outputIndex: Int): IntArray? {
        return getTensorShape(instance, getOutputTensorIndex(instance, outputIndex))
    }

    fun getInputTensorType(inputIndex: Int): Int {
        return getTensorType(instance, getInputTensorIndex(instance, inputIndex))
    }

    fun getOutputTensorType(outputIndex: Int): Int {
        return getTensorType(instance, getOutputTensorIndex(instance, outputIndex))
    }

    fun setTensorData(tensorIndex: Int, data: ByteArray): Boolean {
        val result = setTensorData(instance, data, tensorIndex)
        if (result != AILIA_TFLITE_STATUS_SUCCESS) {
            Log.e(tag, "Failed to set input tensor data: $result")
            return false
        }
        return true
    }

    fun getTensorData(tensorIndex: Int): ByteArray? {
        return getTensorDataAsBytes(instance, tensorIndex)
    }

    fun predict(): Boolean {
        val result = predict(instance)
        if (result != AILIA_TFLITE_STATUS_SUCCESS) {
            Log.e(tag, "Failed to predict: $result")
            return false
        }
        return true
    }

    fun setProfileMode(mode: Int): Boolean {
        val result = setProfileMode(instance, mode)
        if (result != AILIA_TFLITE_STATUS_SUCCESS) {
            Log.e(tag, "Failed to set profile mode: $result")
            return false
        }
        return true
    }

    fun getSummary(): String? {
        return getSummary(instance)
    }

    fun getErrorDetail(): String? {
        return getErrorDetail(instance)
    }

    fun getVersionString(): String {
        return getVersion()
    }

    fun getCpuFeatures(): Int {
        val result = getCpuFeatures(instance)
        if (result < 0) {
            Log.e(tag, "Failed to get CPU features")
            return AILIA_TFLITE_CPU_FEATURES_NONE
        }
        return result
    }

    fun setCpuFeatures(cpuFeatures: Int): Boolean {
        val result = setCpuFeatures(instance, cpuFeatures)
        if (result != AILIA_TFLITE_STATUS_SUCCESS) {
            Log.e(tag, "Failed to set CPU features: $result")
            return false
        }
        return true
    }

    fun getTensorDim(tensorIndex: Int): Int {
        val result = getTensorDim(instance, tensorIndex)
        if (result < 0) {
            Log.e(tag, "Failed to get tensor dimension")
            return 0
        }
        return result
    }

    fun getTensorShapeSignature(tensorIndex: Int): IntArray? {
        return getTensorShapeSignature(instance, tensorIndex)
    }

    fun getTensorName(tensorIndex: Int): String? {
        return getTensorName(instance, tensorIndex)
    }

    fun getOperatorCode(nodeIndex: Int): Int {
        val result = getOperatorCode(instance, nodeIndex)
        if (result < 0) {
            Log.e(tag, "Failed to get operator code")
            return -1
        }
        return result
    }

    fun getTensorQuantizationCount(tensorIndex: Int): Int {
        if (instance == 0L) {
            Log.e(tag, "getTensorQuantizationCount: Instance not created")
            return -1
        }
        val result = getTensorQuantizationCount(instance, tensorIndex)
        if (result < 0) {
            Log.e(tag, "getTensorQuantizationCount failed for tensor $tensorIndex")
        }
        return result
    }

    fun getTensorQuantizationScale(tensorIndex: Int): FloatArray? {
        if (instance == 0L) {
            Log.e(tag, "getTensorQuantizationScale: Instance not created")
            return null
        }
        val result = getTensorQuantizationScale(instance, tensorIndex)
        if (result == null) {
            Log.e(tag, "getTensorQuantizationScale failed for tensor $tensorIndex")
        }
        return result
    }

    fun getTensorQuantizationZeroPoint(tensorIndex: Int): LongArray? {
        if (instance == 0L) {
            Log.e(tag, "getTensorQuantizationZeroPoint: Instance not created")
            return null
        }
        val result = getTensorQuantizationZeroPoint(instance, tensorIndex)
        if (result == null) {
            Log.e(tag, "getTensorQuantizationZeroPoint failed for tensor $tensorIndex")
        }
        return result
    }

    fun getTensorQuantizationQuantizedDimension(tensorIndex: Int): Int {
        if (instance == 0L) {
            Log.e(tag, "getTensorQuantizationQuantizedDimension: Instance not created")
            return -1
        }
        val result = getTensorQuantizationQuantizedDimension(instance, tensorIndex)
        if (result < 0) {
            Log.e(tag, "getTensorQuantizationQuantizedDimension failed for tensor $tensorIndex")
        }
        return result
    }
    fun close() {
        if (instance != 0L) {
            destroy(instance)
            instance = 0
        }
    }

    private external fun getEnvironmentCount(): Int
    private external fun getEnvironment(count: Int): IntArray
    private external fun create(modelData: ByteArray, envId: Int, memoryMode: Int, flags: Int): Long
    private external fun destroy(instance: Long)
    private external fun getCpuFeatures(instance: Long): Int
    private external fun setCpuFeatures(instance: Long, cpuFeatures: Int): Int
    private external fun allocateTensors(instance: Long): Int
    private external fun resizeInputTensor(instance: Long, inputIndex: Int, shape: IntArray, dim: Int): Int
    private external fun getNumberOfInputs(instance: Long): Int
    private external fun getInputTensorIndex(instance: Long, inputIndex: Int): Int
    private external fun getNumberOfOutputs(instance: Long): Int
    private external fun getOutputTensorIndex(instance: Long, outputIndex: Int): Int
    private external fun getTensorShape(instance: Long, tensorIndex: Int): IntArray
    private external fun getTensorType(instance: Long, tensorIndex: Int): Int
    private external fun setTensorData(instance: Long, data: ByteArray, tensorIndex: Int): Int
    private external fun getTensorDataAsBytes(instance: Long, tensorIndex: Int): ByteArray
    private external fun predict(instance: Long): Int
    private external fun setProfileMode(instance: Long, mode: Int): Int
    private external fun getSummary(instance: Long): String
    private external fun getErrorDetail(instance: Long): String
    private external fun getVersion(): String
    private external fun getTensorDim(instance: Long, tensorIndex: Int): Int
    private external fun getTensorShapeSignature(instance: Long, tensorIndex: Int): IntArray
    private external fun getTensorName(instance: Long, tensorIndex: Int): String
    private external fun getOperatorCode(instance: Long, nodeIndex: Int): Int
    private external fun getOperatorName(op: Int): String
    private external fun mklSetNumThreads(numThreads: Int): Int
    private external fun getTensorQuantizationCount(instance: Long, tensorIndex: Int): Int
    private external fun getTensorQuantizationScale(instance: Long, tensorIndex: Int): FloatArray?
    private external fun getTensorQuantizationZeroPoint(instance: Long, tensorIndex: Int): LongArray?
    private external fun getTensorQuantizationQuantizedDimension(instance: Long, tensorIndex: Int): Int

    private external fun mklDisableFastMM(): Int
}
