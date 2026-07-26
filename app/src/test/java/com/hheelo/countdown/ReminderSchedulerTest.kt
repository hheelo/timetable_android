package com.hheelo.countdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReminderSchedulerTest {
    private val event = CountdownEvent(
        id = "event",
        title = "Event",
        targetDate = "2026-07-10",
        reminderEnabled = true,
        reminderDaysBefore = 3
    )

    @Test
    fun deliversOnConfiguredReminderDate() {
        assertTrue(ReminderScheduler.shouldDeliver(event, LocalDate.of(2026, 7, 7)))
    }

    @Test
    fun catchesUpAfterReminderDateBeforeTarget() {
        assertTrue(ReminderScheduler.shouldDeliver(event, LocalDate.of(2026, 7, 9)))
    }

    @Test
    fun skipsBeforeReminderDateAndAfterTarget() {
        assertFalse(ReminderScheduler.shouldDeliver(event, LocalDate.of(2026, 7, 6)))
        assertFalse(ReminderScheduler.shouldDeliver(event, LocalDate.of(2026, 7, 11)))
    }

    @Test
    fun skipsDisabledReminder() {
        assertFalse(
            ReminderScheduler.shouldDeliver(
                event.copy(reminderEnabled = false),
                LocalDate.of(2026, 7, 7)
            )
        )
    }

    @Test
    fun keepsDeliveryRecordsInsideRetentionWindow() {
        val today = LocalDate.of(2026, 7, 10)
        val keys = setOf(
            "event|2026-07-10",
            "event|${today.minusDays(ReminderDeliveryStore.RETENTION_DAYS)}"
        )
        assertEquals(keys, ReminderDeliveryStore.pruned(keys, today))
    }

    @Test
    fun dropsDeliveryRecordsOutsideRetentionWindow() {
        val today = LocalDate.of(2026, 7, 10)
        val stale = "event|${today.minusDays(ReminderDeliveryStore.RETENTION_DAYS + 1)}"
        val fresh = "event|2026-07-09"
        assertEquals(setOf(fresh), ReminderDeliveryStore.pruned(setOf(stale, fresh), today))
    }

    @Test
    fun dropsDeliveryRecordsWithUnparsableDate() {
        val today = LocalDate.of(2026, 7, 10)
        val keys = setOf("event|not-a-date", "event", "event|2026-07-09")
        assertEquals(setOf("event|2026-07-09"), ReminderDeliveryStore.pruned(keys, today))
    }

    @Test
    fun keepsDeliveryRecordsWhenEventIdContainsSeparator() {
        val today = LocalDate.of(2026, 7, 10)
        val keys = setOf("odd|id|2026-07-09")
        assertEquals(keys, ReminderDeliveryStore.pruned(keys, today))
    }
}
