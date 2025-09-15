package com.sos.chakhaeng.recording

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

fun Context.startCameraFgService() {
    val intent = Intent(this, CameraRecordingService::class.java).apply {
        action = CameraRecordingService.ACTION_START
    }
    ContextCompat.startForegroundService(this, intent)
}

fun Context.stopCameraFgService() {
    val intent = Intent(this, CameraRecordingService::class.java).apply {
        action = CameraRecordingService.ACTION_STOP
    }
    startService(intent)
}