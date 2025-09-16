package com.sos.chakhaeng.core.utils.camera

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.ArrayDeque
import kotlin.math.max

data class EncodedSample(val data: ByteArray, val timeUs: Long, val flags: Int)

class RollingEncoder(
    private val context: Context,
    private val width: Int,
    private val height: Int,
    private val fps: Int = 30,
    private val iFrameSec: Int = 2,
    private val bitrate: Int = width * height * 4,
    private val maxBufferSec: Int = 30,
) {
    lateinit var inputSurface: Surface; private set
    private lateinit var codec: MediaCodec
    private val cbThread = HandlerThread("enc-cb").apply { start() }
    private val cbHandler = Handler(cbThread.looper)

    private val lock = Any()
    private val samples = ArrayDeque<EncodedSample>()
    @Volatile private var latestPtsUs = 0L
    private var csd0: ByteArray? = null
    private var csd1: ByteArray? = null

    fun start() {
        val mime = MediaFormat.MIMETYPE_VIDEO_AVC
        val fmt = MediaFormat.createVideoFormat(mime, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameSec)
            setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        }
        codec = MediaCodec.createEncoderByType(mime)
        codec.setCallback(object : MediaCodec.Callback() {
            override fun onOutputFormatChanged(p0: MediaCodec, p1: MediaFormat) {
                csd0 = p1.getByteBuffer("csd-0")?.let { val b = ByteArray(it.remaining()); it.get(b); b }
                csd1 = p1.getByteBuffer("csd-1")?.let { val b = ByteArray(it.remaining()); it.get(b); b }
            }

            override fun onOutputBufferAvailable(
                c: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                val buf = c.getOutputBuffer(index) ?: return c.releaseOutputBuffer(index, false)
                val bytes = ByteArray(info.size)
                buf.position(info.offset)
                buf.limit(info.offset + info.size)
                buf.get(bytes)
                c.releaseOutputBuffer(index, false)

                latestPtsUs = info.presentationTimeUs
                val cutOff = info.presentationTimeUs - maxBufferSec * 1_000_000L
                synchronized(lock) {
                    samples.addLast(EncodedSample(bytes, info.presentationTimeUs, info.flags))
                    while (samples.isNotEmpty() && samples.first().timeUs < cutOff) samples.removeFirst()
                }
            }

            override fun onInputBufferAvailable(c: MediaCodec, index: Int) {

            }

            override fun onError(c: MediaCodec, e: MediaCodec.CodecException) {
               e.printStackTrace()
            }
        }, cbHandler)
        codec.configure(fmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = codec.createInputSurface()
        codec.start()
    }

    fun stop() {
        runCatching { codec.stop() }; runCatching { codec.release() }; cbThread.quitSafely()
    }

    // 트리거 발동 시 호출 : 앞 10, 뒤 10, 총 20초의 클립을 생성해 content Uri 반환
    suspend fun captureClip(preSec: Int = 10, postSec: Int = 10): Uri =
        withContext(Dispatchers.IO) {
            val targetEnd = latestPtsUs + postSec * 1_000_000L
            while (latestPtsUs < targetEnd) delay(60)

            val endUs = latestPtsUs
            val startUs = endUs - (preSec + postSec) * 1_000_000L

            val window: List<EncodedSample> = synchronized(lock) {
                samples.filter { it.timeUs in startUs..endUs }
            }
            require(window.isNotEmpty()) { "No samples in window" }

            // 시작을 가장 가까운 이전 키프레임으로 당김
            var startIdx = window.indexOfFirst { it.timeUs > startUs }.coerceAtLeast(0)
            while (startIdx > 0 && (window[startIdx].flags and MediaCodec.BUFFER_FLAG_KEY_FRAME) == 0) startIdx--
            val trimmed = window.drop(startIdx)
            require(trimmed.isNotEmpty()) { "No keyframe in window" }

            // MP4 mux
            val out = File(context.cacheDir, "clip_${System.currentTimeMillis()}.mp4")
            val muxer = MediaMuxer(out.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val trackFmt = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                csd0?.let { setByteBuffer("csd-0", ByteBuffer.wrap(it)) }
                csd1?.let { setByteBuffer("csd-1", ByteBuffer.wrap(it)) }
            }
            val vTrack = muxer.addTrack(trackFmt)
            muxer.start()

            val basePts = trimmed.first().timeUs
            val info = MediaCodec.BufferInfo()
            for (s in trimmed) {
                val buf = ByteBuffer.wrap(s.data)
                info.set(0, s.data.size, max(0, s.timeUs  - basePts), s.flags)
                muxer.writeSampleData(vTrack, buf, info)
            }
            muxer.stop(); muxer.release()

            FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", out
            )
        }



}