package com.geek.mrguard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import com.geek.mrguard.UI.MainActivity
import com.geek.mrguard.UI.dashBoard.Police.PoliceDashBoard
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import androidx.localbroadcastmanager.content.LocalBroadcastManager




class MyFirebaseInstanceIDService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // TODO(developer): Handle FCM messages here.
        Log.d(Companion.TAG, "From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(Companion.TAG, "Message data payload: ${remoteMessage.data}")

        }
        remoteMessage.notification?.let {
            Log.d(Companion.TAG, "Message Notification Body: ${it.body}")
            it.body?.let { it1 -> sendNotification(it1) }
            val intent = Intent("myFunction")
            intent.putExtra("body", it.body)
            intent.putExtra("roomID", remoteMessage.data["roomId"])
            intent.putExtra("victimPhoneNumber", remoteMessage.data["victimProfile"])
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }
    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.fcm_fallback_notification_channel_label)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.cap)
            .setContentTitle("Emergency")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
    companion object {
        const val TAG = "firebase fcm"
    }
}