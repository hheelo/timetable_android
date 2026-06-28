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

        var notified = 0
        for (event in events) {
            if (!event.reminderEnabled) continue

            val targetDate = runCatching { LocalDate.parse(event.targetDate) }.getOrNull() ?: continue
            val reminderDate = targetDate.minusDays(event.reminderDaysBefore.toLong())

            if (today == reminderDate) {
                val daysRemaining = ChronoUnit.DAYS.between(today, targetDate)
                NotificationHelper.postReminder(
                    applicationContext,
                    event.id,
                    event.title,
                    daysRemaining
                )
                notified++
            }
        }

        AppLog.i(TAG, "ReminderWorker 完成，发送了 $notified 条提醒")
        return Result.success()
    }

    companion object {
        private const val TAG = "ReminderWorker"
        const val WORK_NAME = "daily_reminder_check"
    }
}
