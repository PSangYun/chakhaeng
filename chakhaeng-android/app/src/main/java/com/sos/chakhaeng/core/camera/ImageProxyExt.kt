package com.sos.chakhaeng.core.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy

fun ImageProxy.toBitmap(converter: YuvToRgbConverter): Bitmap {
    val bmp = converter.yuv420ToBitmap(this)
    this.close()
    return bmp
}