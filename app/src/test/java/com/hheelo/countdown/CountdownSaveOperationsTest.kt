package com.hheelo.countdown

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CountdownSaveOperationsTest {

    @Test
    fun successfulSaveRunsEveryStageInOrder() = runBlocking {
        val calls = mutableListOf<String>()

        val result = executeCountdownSave(
            persistEvents = { calls += "persist" },
            syncReminders = { calls += "reminders" },
            refreshWidget = { calls += "widget" }
        )

        assertTrue(result.dataSaved)
        assertTrue(result.fullySynchronized)
        assertEquals(listOf("persist", "reminders", "widget"), calls)
    }

    @Test
    fun persistenceFailureSkipsDownstreamSynchronization() = runBlocking {
        val failure = IllegalStateException("disk full")
        var remindersCalled = false
        var widgetCalled = false

        val result = executeCountdownSave(
            persistEvents = { throw failure },
            syncReminders = { remindersCalled = true },
            refreshWidget = { widgetCalled = true }
        )

        assertSame(failure, result.persistenceError)
        assertFalse(result.dataSaved)
        assertFalse(remindersCalled)
        assertFalse(widgetCalled)
    }

    @Test
    fun reminderFailureDoesNotPreventWidgetRefresh() = runBlocking {
        val failure = IllegalStateException("work manager unavailable")
        var widgetCalled = false

        val result = executeCountdownSave(
            persistEvents = {},
            syncReminders = { throw failure },
            refreshWidget = { widgetCalled = true }
        )

        assertTrue(result.dataSaved)
        assertSame(failure, result.reminderSyncError)
        assertNull(result.widgetRefreshError)
        assertTrue(widgetCalled)
        assertFalse(result.fullySynchronized)
    }

    @Test
    fun widgetFailureIsReportedAfterReminderSync() = runBlocking {
        val failure = IllegalStateException("widget host unavailable")
        var remindersCalled = false

        val result = executeCountdownSave(
            persistEvents = {},
            syncReminders = { remindersCalled = true },
            refreshWidget = { throw failure }
        )

        assertTrue(result.dataSaved)
        assertTrue(remindersCalled)
        assertNull(result.reminderSyncError)
        assertSame(failure, result.widgetRefreshError)
        assertFalse(result.fullySynchronized)
    }

    @Test
    fun bothSynchronizationFailuresAreReported() = runBlocking {
        val reminderFailure = IllegalStateException("reminder failure")
        val widgetFailure = IllegalStateException("widget failure")

        val result = executeCountdownSave(
            persistEvents = {},
            syncReminders = { throw reminderFailure },
            refreshWidget = { throw widgetFailure }
        )

        assertTrue(result.dataSaved)
        assertSame(reminderFailure, result.reminderSyncError)
        assertSame(widgetFailure, result.widgetRefreshError)
        assertFalse(result.fullySynchronized)
    }

    @Test
    fun statusMessagesDistinguishPersistenceAndSynchronizationFailures() {
        val failure = IllegalStateException("failure")

        assertEquals(R.string.save_success_message, CountdownSaveResult().statusMessageRes())
        assertEquals(
            R.string.save_failure_message,
            CountdownSaveResult(persistenceError = failure).statusMessageRes()
        )
        assertEquals(
            R.string.save_reminder_sync_failure_message,
            CountdownSaveResult(reminderSyncError = failure).statusMessageRes()
        )
        assertEquals(
            R.string.save_widget_refresh_failure_message,
            CountdownSaveResult(widgetRefreshError = failure).statusMessageRes()
        )
        assertEquals(
            R.string.save_reminder_and_widget_failure_message,
            CountdownSaveResult(
                reminderSyncError = failure,
                widgetRefreshError = failure
            ).statusMessageRes()
        )
    }

    @Test
    fun cancellationIsNotConvertedIntoARegularFailure() {
        try {
            runBlocking {
                executeCountdownSave(
                    persistEvents = { throw CancellationException("cancelled") },
                    syncReminders = {},
                    refreshWidget = {}
                )
            }
            fail("Expected cancellation")
        } catch (_: CancellationException) {
            // Expected: structured cancellation must remain observable by the caller.
        }
    }
}
