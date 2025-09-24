package com.sos.chakhaeng

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class ChakHaengApplication : Application(), TextToSpeech.OnInitListener, Configuration.Provider{

    companion object {
        lateinit var tts: TextToSpeech
        var ttsReady = false
    }
    @Inject lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        tts = TextToSpeech(this, this)
    }
    private fun createNotificationChannels() {
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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.KOREAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("App", "TTS 한국어 미지원")
            } else {
                ttsReady = true
            }
        } else {
            Log.e("App", "TTS 초기화 실패")
        }
    }
    override fun onTerminate() {
        super.onTerminate()
    }


}