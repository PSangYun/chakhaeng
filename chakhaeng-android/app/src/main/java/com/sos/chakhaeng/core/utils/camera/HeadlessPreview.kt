package com.sos.chakhaeng.core.utils.camera

import android.graphics.SurfaceTexture
import android.view.Surface

class HeadlessPreview(
    private val width: Int,
    private val height: Int
) {
    private var surfaceTexture: SurfaceTexture? = null
    var surface: Surface? = null
        private set

    fun create() {
        // GL context 없이도 생성 가능. 버퍼 크기만 지정.
        val st = SurfaceTexture(0)
        st.setDefaultBufferSize(width, height)
        surfaceTexture = st
        surface = Surface(st)
    }

    fun release() {
        runCatching { surface?.release() }
        runCatching { surfaceTexture?.release() }
        surface = null
        surfaceTexture = null
    }
}