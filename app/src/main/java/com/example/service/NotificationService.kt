package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R

object NotificationService {
  const val CHANNEL_MESSAGES = "ffchat_messages"
  const val CHANNEL_CALLS = "ffchat_calls"
  const val CHANNEL_GROUPS = "ffchat_groups"

  fun initChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      val msgChannel = NotificationChannel(
        CHANNEL_MESSAGES,
        "Direct & Group Messages",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Instant notifications for messages and replies"
        enableVibration(true)
      }

      val callChannel = NotificationChannel(
        CHANNEL_CALLS,
        "Voice & Video Calls",
        NotificationManager.IMPORTANCE_MAX
      ).apply {
        description = "Incoming voice and video call alerts"
        enableVibration(true)
      }

      val groupChannel = NotificationChannel(
        CHANNEL_GROUPS,
        "Group Alerts & Invites",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Group promotions, invites, and membership changes"
      }

      notificationManager.createNotificationChannel(msgChannel)
      notificationManager.createNotificationChannel(callChannel)
      notificationManager.createNotificationChannel(groupChannel)
    }
  }

  fun showNotification(
    context: Context,
    channelId: String,
    title: String,
    content: String,
    notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
  ) {
    try {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

      notificationManager.notify(notificationId, builder.build())
    } catch (_: Exception) {
      // Gracefully handle if permission is not yet granted
    }
  }
}
