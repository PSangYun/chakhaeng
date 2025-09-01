package com.sos.chakhaeng

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ChakHaengApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 앱 초기화 로직
    }
}