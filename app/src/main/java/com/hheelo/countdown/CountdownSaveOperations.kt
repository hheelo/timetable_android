package com.hheelo.countdown

import kotlinx.coroutines.CancellationException

internal data class CountdownSaveResult(
    val persistenceError: Throwable? = null,
    val reminderSyncError: Throwable? = null,
    val widgetRefreshError: Throwable? = null
) {
    val dataSaved: Boolean
        get() = persistenceError == null

    val fullySynchronized: Boolean
        get() = dataSaved && reminderSyncError == null && widgetRefreshError == null
}

/**
 * Persists events first, then runs reminder and widget synchronization independently.
 *
 * A persistence failure stops downstream work. Once data is safely stored, a failure in either
 * synchronization step does not prevent the other step from being attempted.
 */
internal suspend fun executeCountdownSave(
    persistEvents: suspend () -> Unit,
    syncReminders: suspend () -> Unit,
    refreshWidget: suspend () -> Unit
): CountdownSaveResult {
    val persistenceError = captureFailure(persistEvents)
    if (persistenceError != null) {
        return CountdownSaveResult(persistenceError = persistenceError)
    }

    val reminderSyncError = captureFailure(syncReminders)
    val widgetRefreshError = captureFailure(refreshWidget)
    return CountdownSaveResult(
        reminderSyncError = reminderSyncError,
        widgetRefreshError = widgetRefreshError
    )
}

internal fun CountdownSaveResult.statusMessageRes(): Int {
    return when {
        persistenceError != null -> R.string.save_failure_message
        reminderSyncError != null && widgetRefreshError != null ->
            R.string.save_reminder_and_widget_failure_message
        reminderSyncError != null -> R.string.save_reminder_sync_failure_message
        widgetRefreshError != null -> R.string.save_widget_refresh_failure_message
        else -> R.string.save_success_message
    }
}

private suspend fun captureFailure(operation: suspend () -> Unit): Throwable? {
    return try {
        operation()
        null
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        throwable
    }
}
