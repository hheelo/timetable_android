package com.hheelo.countdown

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.hheelo.countdown.logging.AppLog

object NotificationHelper {

    private const val CHANNEL_ID = "countdown_reminders"
    private const val TAG = "NotificationHelper"

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

    fun canPostNotifications(context: Context): Boolean {
        val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!runtimePermissionGranted || !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = manager.getNotificationChannel(CHANNEL_ID)
        return channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun postReminder(context: Context, eventId: String, title: String, daysRemaining: Long): Boolean {
        if (!canPostNotifications(context)) {
            AppLog.w(TAG, "通知权限未授予，跳过事件提醒 eventId=$eventId")
            return false
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val text = if (daysRemaining == 0L) {
            context.getString(R.string.reminder_notification_today, title)
        } else {
            context.getString(R.string.reminder_notification_text, title, daysRemaining.toInt())
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            Intent(Intent.ACTION_VIEW, Uri.parse(AppDeepLink.eventUrl(eventId))).apply {
                setPackage(context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(text)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        val notificationId = eventId.hashCode()
        return runCatching {
            notificationManager.notify(notificationId, notification)
            true
        }.onFailure {
            AppLog.e(TAG, "发送事件提醒失败 eventId=$eventId", it)
        }.getOrDefault(false)
    }
}
