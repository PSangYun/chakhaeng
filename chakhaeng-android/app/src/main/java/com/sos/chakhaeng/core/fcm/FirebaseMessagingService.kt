package com.sos.chakhaeng.core.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sos.chakhaeng.ChakHaengApplication
import com.sos.chakhaeng.R
import com.sos.chakhaeng.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "ChakengNotificationService"
        private const val CHANNEL_ID = "chakeng_notifications"
    }

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data.isNotEmpty()) {
            sendNotification(message)
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        Log.d("test2134", data.toString())

        val intent = Intent(this, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("title", remoteMessage.notification?.title)
        }
        val pending = PendingIntent.getActivity(
            this,
            (System.currentTimeMillis() / 7).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "Notice_Notification"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(channelId, "Notice", NotificationManager.IMPORTANCE_DEFAULT)
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setAutoCancel(true)
            .setContentIntent(pending)

        nm.notify((System.currentTimeMillis() / 7).toInt(), builder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "new token : $token")
        fcmTokenManager.handleNewToken(token)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "착행 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "착행 앱의 알림"
            enableVibration(true)
            setShowBadge(true)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}
