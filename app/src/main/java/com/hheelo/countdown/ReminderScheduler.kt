package com.hheelo.countdown

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.hheelo.countdown.logging.AppLog
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"
    private const val WORK_NAME_PREFIX = "event_reminder_"
    private const val SCHEDULED_IDS_KEY = "scheduled_event_ids"
    private const val SCHEDULER_PREFERENCES = "reminder_scheduler"
    private val deliveryTime = LocalTime.of(9, 0)

    fun sync(
        context: Context,
        events: List<CountdownEvent>,
        now: ZonedDateTime = ZonedDateTime.now()
    ) {
        val appContext = context.applicationContext
        val workManager = WorkManager.getInstance(appContext)
        val preferences = appContext.getSharedPreferences(SCHEDULER_PREFERENCES, Context.MODE_PRIVATE)
        val previouslyScheduled = preferences.getStringSet(SCHEDULED_IDS_KEY, emptySet()).orEmpty()

        workManager.cancelUniqueWork(ReminderWorker.LEGACY_PERIODIC_WORK_NAME)

        val schedulable = events.mapNotNull { event ->
            val targetDate = parseTargetDate(event) ?: return@mapNotNull null
            if (!event.reminderEnabled || targetDate.isBefore(now.toLocalDate())) return@mapNotNull null

            val reminderDate = targetDate.minusDays(event.reminderDaysBefore.toLong())
            if (ReminderDeliveryStore.wasDelivered(appContext, event.id, reminderDate)) {
                return@mapNotNull null
            }
            ScheduledReminder(event, reminderDate)
        }
        val desiredIds = schedulable.mapTo(mutableSetOf()) { it.event.id }

        (previouslyScheduled - desiredIds).forEach { eventId ->
            workManager.cancelUniqueWork(workName(eventId))
        }

        schedulable.forEach { scheduled ->
            val triggerAt = scheduled.reminderDate.atTime(deliveryTime).atZone(now.zone)
            val delayMillis = Duration.between(now.toInstant(), triggerAt.toInstant())
                .toMillis()
                .coerceAtLeast(0L)
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(workDataOf(ReminderWorker.EVENT_ID_KEY to scheduled.event.id))
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 6, TimeUnit.HOURS)
                .addTag(WORK_NAME_PREFIX)
                .build()

            workManager.enqueueUniqueWork(
                workName(scheduled.event.id),
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        preferences.edit().putStringSet(SCHEDULED_IDS_KEY, desiredIds).apply()
        AppLog.i(TAG, "已同步 ${desiredIds.size} 个事件提醒")
    }

    internal fun shouldDeliver(event: CountdownEvent, today: LocalDate): Boolean {
        if (!event.reminderEnabled) return false
        val targetDate = parseTargetDate(event) ?: return false
        val reminderDate = targetDate.minusDays(event.reminderDaysBefore.toLong())
        return !today.isBefore(reminderDate) && !today.isAfter(targetDate)
    }

    internal fun reminderDate(event: CountdownEvent): LocalDate? {
        return parseTargetDate(event)?.minusDays(event.reminderDaysBefore.toLong())
    }

    private fun parseTargetDate(event: CountdownEvent): LocalDate? {
        return runCatching { LocalDate.parse(event.targetDate) }.getOrNull()
    }

    private fun workName(eventId: String): String = WORK_NAME_PREFIX + eventId

    private data class ScheduledReminder(
        val event: CountdownEvent,
        val reminderDate: LocalDate
    )
}

internal object ReminderDeliveryStore {
    private const val PREFERENCES_NAME = "reminder_delivery"
    private const val DELIVERED_KEYS = "delivered_keys"

    @Synchronized
    fun wasDelivered(context: Context, eventId: String, reminderDate: LocalDate): Boolean {
        return deliveryKey(eventId, reminderDate) in deliveredKeys(context)
    }

    @Synchronized
    fun markDelivered(context: Context, eventId: String, reminderDate: LocalDate) {
        val updated = deliveredKeys(context).toMutableSet().apply {
            add(deliveryKey(eventId, reminderDate))
        }
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(DELIVERED_KEYS, updated)
            .apply()
    }

    private fun deliveredKeys(context: Context): Set<String> {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getStringSet(DELIVERED_KEYS, emptySet())
            .orEmpty()
    }

    private fun deliveryKey(eventId: String, reminderDate: LocalDate): String {
        return "$eventId|$reminderDate"
    }
}
