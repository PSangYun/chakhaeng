package com.sos.chakhaeng.core.utils

import android.media.MediaCodec
import android.os.HandlerThread
import android.view.Surface
import com.google.api.Context

data class EncodedSample(val data: ByteArray, val timeUs: Long, val flags: Int)

class RollingEncoder(
    private val context: Context,
    private val width: Int,
    private val height: Int,
    private val fps: Int = 30,
    private val iFrameSec: Int = 2,
    private val bitRate: Int = width * height * 4,
    private val maxBufferSec: Int = 30,
) {
    lateinit var inputSurface: Surface; private set
    private lateinit var codec: MediaCodec
    private val cbThread = HandlerThread("enc-cb")
}