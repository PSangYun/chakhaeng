package com.sos.chakhaeng

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ChakHaengApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        // 앱 초기화 로직
    }
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "violation",
                "Violation Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "위반 감지 알림"
                enableVibration(true)
            }
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}