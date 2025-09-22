package com.sos.chakhaeng.core.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Activity.enterImmersive() {
    val w = window
    WindowCompat.setDecorFitsSystemWindows(w, false)
    val c = WindowCompat.getInsetsController(w, w.decorView)
    c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    c.hide(WindowInsetsCompat.Type.systemBars())
}

fun Activity.exitImmersive() {
    val w = window
    WindowCompat.setDecorFitsSystemWindows(w, true)
    val c = WindowCompat.getInsetsController(w, w.decorView)
    c.show(WindowInsetsCompat.Type.systemBars())
}
fun Activity.setFullscreen(enabled: Boolean) {
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    if (enabled) {
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    } else {
        controller.show(WindowInsetsCompat.Type.systemBars())
    }
}
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}