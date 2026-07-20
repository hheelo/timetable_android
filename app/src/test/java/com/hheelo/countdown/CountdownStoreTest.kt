package com.hheelo.countdown

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class CountdownStoreTest {
    private lateinit var context: Context
    private lateinit var store: CountdownStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("countdown_store", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        store = CountdownStore(context)
    }

    // --- Sorting logic ---

    @Test
    fun pinnedEventsSortBeforeUnpinned() {
        val events = listOf(
            CountdownEvent(id = "unpinned", title = "Unpinned", targetDate = "2026-07-01", isPinned = false),
            CountdownEvent(id = "pinned", title = "Pinned", targetDate = "2026-08-01", isPinned = true)
        )

        store.saveCustomEvents(events)
        val loaded = store.loadCustomEvents()

        assertEquals("pinned", loaded[0].id)
        assertEquals("unpinned", loaded[1].id)
    }

    @Test
    fun pinnedEventsKeepManualSortOrder() {
        val events = listOf(
            CountdownEvent(id = "pinB", title = "Pin B", targetDate = "2026-07-01", isPinned = true, sortOrder = 1),
            CountdownEvent(id = "pinA", title = "Pin A", targetDate = "2026-08-01", isPinned = true, sortOrder = 0)
        )

        store.saveCustomEvents(events)
        val loaded = store.loadCustomEvents()

        // sortOrder is reassigned based on list index during normalization,
        // so the input order becomes the sort order: pinB=0, pinA=1
        assertEquals("pinB", loaded[0].id)
        assertEquals("pinA", loaded[1].id)
    }

    @Test
    fun unpinnedEventsKeepManualOrder() {
        val events = listOf(
            CountdownEvent(id = "far", title = "Far", targetDate = "2026-12-31", isPinned = false),
            CountdownEvent(id = "near", title = "Near", targetDate = "2026-07-01", isPinned = false),
            CountdownEvent(id = "mid", title = "Mid", targetDate = "2026-09-15", isPinned = false)
        )

        store.saveCustomEvents(events)
        val loaded = store.loadCustomEvents()

        assertEquals(listOf("far", "near", "mid"), loaded.map { it.id })
        assertEquals(listOf(0, 1, 2), loaded.map { it.sortOrder })
    }

    @Test
    fun pinnedAndUnpinnedGroupsBothKeepManualOrder() {
        val events = listOf(
            CountdownEvent(id = "unpin-late", title = "Late", targetDate = "2026-12-25", isPinned = false),
            CountdownEvent(id = "pin-second", title = "Pin 2", targetDate = "2026-07-04", isPinned = true),
            CountdownEvent(id = "unpin-early", title = "Early", targetDate = "2026-07-01", isPinned = false),
            CountdownEvent(id = "pin-first", title = "Pin 1", targetDate = "2026-11-01", isPinned = true)
        )

        store.saveCustomEvents(events)
        val loaded = store.loadCustomEvents()

        // Pinned events come first, in their input (manual) order
        assertEquals("pin-second", loaded[0].id)
        assertEquals("pin-first", loaded[1].id)
        // Unpinned events follow in their input (manual) order
        assertEquals("unpin-late", loaded[2].id)
        assertEquals("unpin-early", loaded[3].id)
        assertEquals(listOf(0, 1, 2, 3), loaded.map { it.sortOrder })
    }

    // --- Normalization ---

    @Test
    fun titleIsTrimmed() {
        val events = listOf(
            CountdownEvent(id = "1", title = "  Hello World  ", targetDate = "2026-07-01")
        )

        store.saveCustomEvents(events)
        val loaded = store.loadCustomEvents()

        assertEquals("Hello World", loaded[0].title)
    }

    @Test
    fun emptyTitleGetsDefaultName() {
        val events = listOf(
            CountdownEvent(id = "1", title = "", targetDate = "2026-07-01"),
            CountdownEvent(id = "2", title = "   ", targetDate = "2026-07-02")
        )

        store.saveCustomEvents(events)
        val loaded = store.loadCustomEvents()

        val defaultName = context.getString(R.string.unnamed_event)
        assertEquals(defaultName, loaded[0].title)
        assertEquals(defaultName, loaded[1].title)
    }

    @Test
    fun invalidDatesAreFiltered() {
        val events = listOf(
            CountdownEvent(id = "valid", title = "Valid", targetDate = "2026-07-01"),
            CountdownEvent(id = "invalid", title = "Invalid", targetDate = "not-a-date"),
            CountdownEvent(id = "also-valid", title = "Also Valid", targetDate = "2026-08-01")
        )

        store.saveCustomEvents(events)
        val loaded = store.loadCustomEvents()

        assertEquals(2, loaded.size)
        assertEquals(listOf("valid", "also-valid"), loaded.map { it.id })
    }

    @Test
    fun invalidColorIsSanitizedToDefault() {
        val events = listOf(
            CountdownEvent(id = "1", title = "Bad Color", targetDate = "2026-07-01", colorHex = "not-a-color")
        )

        store.saveCustomEvents(events)
        val loaded = store.loadCustomEvents()

        assertEquals(CountdownColorHex.Brand, loaded[0].colorHex)
    }

    @Test
    fun unsupportedReminderDaysFallsBackToOneDay() {
        val events = listOf(
            CountdownEvent(
                id = "1",
                title = "Reminder",
                targetDate = "2026-07-01",
                reminderEnabled = true,
                reminderDaysBefore = 99
            )
        )

        store.saveCustomEvents(events)

        assertEquals(1, store.loadCustomEvents().single().reminderDaysBefore)
    }

    // --- Corruption detection ---

    @Test
    fun malformedJsonLoadsEmptyListWithoutCrashing() {
        // Write malformed JSON directly to SharedPreferences
        val prefs = context.getSharedPreferences("countdown_store", Context.MODE_PRIVATE)
        prefs.edit().putString("customCountdownEvents", "this is not json").commit()

        val loaded = store.loadCustomEvents()

        assertEquals(emptyList<CountdownEvent>(), loaded)
    }

    @Test
    fun invalidElementsInArrayAreSkipped() {
        // Valid JSON array but with one invalid element
        val prefs = context.getSharedPreferences("countdown_store", Context.MODE_PRIVATE)
        val validEvent = """{"id":"good","title":"Good","targetDate":"2026-07-01","colorHex":"#FF6B4A","isPinned":false,"sortOrder":0,"reminderEnabled":false,"reminderDaysBefore":1}"""
        val invalidElement = """{"missing_required_fields": true}"""
        prefs.edit().putString("customCountdownEvents", "[$validEvent,$invalidElement]").commit()

        val loaded = store.loadCustomEvents()

        assertEquals(1, loaded.size)
        assertEquals("good", loaded[0].id)
    }

    @Test
    fun corruptionIsDetectedWhenFieldsAreInvalid() {
        // Store events with an invalid date — normalization will flag hasInvalidFields
        val prefs = context.getSharedPreferences("countdown_store", Context.MODE_PRIVATE)
        val badDateEvent = """{"id":"bad","title":"Bad","targetDate":"invalid-date","colorHex":"#FF6B4A","isPinned":false,"sortOrder":0,"reminderEnabled":false,"reminderDaysBefore":1}"""
        prefs.edit().putString("customCountdownEvents", "[$badDateEvent]").commit()

        store.loadCustomEvents()

        // Verify that a corruption reason was backed up
        val reason = prefs.getString("customCountdownEvents.corruptReason", null)
        assertTrue("Expected corruption reason to be recorded", reason != null)
        assertTrue(reason!!.contains("invalid"))
    }

    @Test
    fun emptyStoredValueTriggersBackupAndReturnsEmptyList() {
        val prefs = context.getSharedPreferences("countdown_store", Context.MODE_PRIVATE)
        prefs.edit().putString("customCountdownEvents", "").commit()

        val loaded = store.loadCustomEvents()

        assertEquals(emptyList<CountdownEvent>(), loaded)
        val reason = prefs.getString("customCountdownEvents.corruptReason", null)
        assertEquals("stored value is empty", reason)
    }

    // --- Color sanitization ---

    @Test
    fun validHexColorPassesThrough() {
        val events = listOf(
            CountdownEvent(id = "1", title = "Test", targetDate = "2026-07-01", colorHex = "#3B82F6")
        )

        store.saveCustomEvents(events)
        val loaded = store.loadCustomEvents()

        assertEquals("#3B82F6", loaded[0].colorHex)
    }

    @Test
    fun invalidHexFallsBackToDefault() {
        val events = listOf(
            CountdownEvent(id = "1", title = "Test", targetDate = "2026-07-01", colorHex = "xyz"),
            CountdownEvent(id = "2", title = "Test2", targetDate = "2026-07-02", colorHex = "#ZZZZZZ"),
            CountdownEvent(id = "3", title = "Test3", targetDate = "2026-07-03", colorHex = "")
        )

        store.saveCustomEvents(events)
        val loaded = store.loadCustomEvents()

        assertEquals(CountdownColorHex.Brand, loaded[0].colorHex)
        assertEquals(CountdownColorHex.Brand, loaded[1].colorHex)
        assertEquals(CountdownColorHex.Brand, loaded[2].colorHex)
    }

    @Test
    fun colorHexWithWhitespaceIsTrimmed() {
        val events = listOf(
            CountdownEvent(id = "1", title = "Test", targetDate = "2026-07-01", colorHex = "  #3B82F6  ")
        )

        store.saveCustomEvents(events)
        val loaded = store.loadCustomEvents()

        assertEquals("#3B82F6", loaded[0].colorHex)
    }

    // --- Round-trip ---

    @Test
    fun saveAndLoadPreservesEventFields() {
        val events = listOf(
            CountdownEvent(
                id = "abc",
                title = "Birthday",
                targetDate = "2026-12-25",
                colorHex = "#F59E0B",
                isPinned = true,
                reminderEnabled = true,
                reminderDaysBefore = 3
            )
        )

        store.saveCustomEvents(events)
        val loaded = store.loadCustomEvents()

        assertEquals(1, loaded.size)
        val event = loaded[0]
        assertEquals("abc", event.id)
        assertEquals("Birthday", event.title)
        assertEquals("2026-12-25", event.targetDate)
        assertEquals("#F59E0B", event.colorHex)
        assertEquals(true, event.isPinned)
        assertEquals(true, event.reminderEnabled)
        assertEquals(3, event.reminderDaysBefore)
    }

    @Test
    fun loadReturnsDefaultEventsWhenNothingSaved() {
        // Fresh store with no saved events
        val freshStore = CountdownStore(context)
        val loaded = freshStore.loadCustomEvents()

        assertEquals(1, loaded.size)
        assertEquals(context.getString(R.string.default_event_title), loaded[0].title)
        assertEquals(CountdownColorHex.Brand, loaded[0].colorHex)
    }

    @Test
    fun savedEmptyListDoesNotRestoreDefaultEvent() {
        val defaultEvents = store.loadCustomEvents()
        assertEquals(1, defaultEvents.size)
        assertEquals(context.getString(R.string.default_event_title), defaultEvents[0].title)

        store.saveCustomEvents(emptyList())

        val loaded = CountdownStore(context).loadCustomEvents()
        assertEquals(emptyList<CountdownEvent>(), loaded)
    }
}
