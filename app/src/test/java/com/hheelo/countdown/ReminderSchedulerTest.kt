package com.hheelo.countdown

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
}
