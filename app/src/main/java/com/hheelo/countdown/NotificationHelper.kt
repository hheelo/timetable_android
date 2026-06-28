package com.hheelo.countdown

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    private const val CHANNEL_ID = "countdown_reminders"

    fun createChannel(context: Context) {
        val channelName = context.getString(R.string.reminder_notification_channel)
        val channel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun postReminder(context: Context, eventId: String, title: String, daysRemaining: Long) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val text = if (daysRemaining == 0L) {
            context.getString(R.string.reminder_notification_today, title)
        } else {
            context.getString(R.string.reminder_notification_text, title, daysRemaining.toInt())
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        val notificationId = eventId.hashCode()
        notificationManager.notify(notificationId, notification)
    }
}
