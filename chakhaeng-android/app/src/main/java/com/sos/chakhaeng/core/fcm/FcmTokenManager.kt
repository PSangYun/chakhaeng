package com.sos.chakhaeng.core.fcm

import android.util.Log
import com.sos.chakhaeng.domain.usecase.auth.SendFcmTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    private val sendFcmTokenUseCase: SendFcmTokenUseCase
) {

    companion object {
        private const val TAG = "FcmTokenManager"
    }

    /**.
     * 새 토큰을 받았을 때 처리 (onNewToken에서 호출)
     */
    fun handleNewToken(token: String) {
        Log.d(TAG, "Handling new FCM token: $token")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendFcmTokenUseCase(token)
                Log.d(TAG, "FCM token refreshed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh FCM token: ${e.message}", e)
            }
        }
    }
}