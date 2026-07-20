package com.hheelo.countdown

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hheelo.countdown.logging.AppLog
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        AppLog.i(TAG, "ReminderWorker 开始检查提醒")
        val store = CountdownStore(applicationContext)
        val events = store.loadCustomEvents()
        val today = LocalDate.now()
        val requestedEventId = inputData.getString(EVENT_ID_KEY)
        val candidates = if (requestedEventId == null) {
            events
        } else {
            events.filter { it.id == requestedEventId }
        }

        var notified = 0
        for (event in candidates) {
            val targetDate = runCatching { LocalDate.parse(event.targetDate) }.getOrNull() ?: continue
            val reminderDate = ReminderScheduler.reminderDate(event) ?: continue

            if (
                ReminderScheduler.shouldDeliver(event, today) &&
                !ReminderDeliveryStore.wasDelivered(applicationContext, event.id, reminderDate)
            ) {
                val daysRemaining = ChronoUnit.DAYS.between(today, targetDate)
                val delivered = NotificationHelper.postReminder(
                    applicationContext,
                    event.id,
                    event.title,
                    daysRemaining
                )
                if (delivered) {
                    ReminderDeliveryStore.markDelivered(applicationContext, event.id, reminderDate)
                    notified++
                }
            }
        }

        AppLog.i(TAG, "ReminderWorker 完成，发送了 $notified 条提醒")
        return Result.success()
    }

    companion object {
        private const val TAG = "ReminderWorker"
        const val EVENT_ID_KEY = "event_id"
        const val LEGACY_PERIODIC_WORK_NAME = "daily_reminder_check"
    }
}
