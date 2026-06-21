package com.hheelo.countdown

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class CountdownCalculatorTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun daysBetweenAllowsFutureAndPastTargets() {
        val from = LocalDate.of(2026, 6, 12)

        assertEquals(2, CountdownCalculator.daysBetween(from, LocalDate.of(2026, 6, 14)))
        assertEquals(-2, CountdownCalculator.daysBetween(from, LocalDate.of(2026, 6, 10)))
    }

    @Test
    fun defaultCardsCountDownToNextSaturdayOnWeekdays() {
        val cards = CountdownCalculator.makeDefaultCards(context, LocalDate.of(2026, 6, 12))

        val weekend = cards.first()
        assertEquals(context.getString(R.string.weekend_title), weekend.title)
        assertEquals(context.getString(R.string.weekend_countdown), weekend.subtitle)
        assertEquals(1, weekend.days)
        assertEquals("weekend", weekend.iconName)
        assertEquals(AppDeepLink.HomeUrl, weekend.deepLink)
    }

    @Test
    fun weekendCardIsDueTodayOnSaturday() {
        val weekend = CountdownCalculator.makeDefaultCards(context, LocalDate.of(2026, 6, 13)).first()

        assertEquals(context.getString(R.string.weekend_today), weekend.subtitle)
        assertEquals(0, weekend.days)
    }

    @Test
    fun holidayCardShowsActiveHolidayWhenTodayIsInHolidayRange() {
        val holiday = CountdownCalculator.makeDefaultCards(context, LocalDate.of(2026, 10, 5))[1]

        assertEquals(context.getString(R.string.holiday_national_day), holiday.title)
        assertEquals(context.getString(R.string.holiday_ongoing), holiday.subtitle)
        assertEquals(0, holiday.days)
        assertEquals("holiday", holiday.iconName)
        assertEquals(AppDeepLink.HomeUrl, holiday.deepLink)
    }

    @Test
    fun holidayCardFallsBackWhenYearHasNoHolidayData() {
        val holiday = CountdownCalculator.makeDefaultCards(context, LocalDate.of(2099, 1, 1))[1]

        assertEquals(context.getString(R.string.holiday_title), holiday.title)
        assertEquals(0L, holiday.days)
        assertTrue(holiday.subtitle.contains(context.getString(R.string.holiday_no_data, 2099).substring(0, 2)))
        assertEquals("holiday", holiday.iconName)
        assertEquals(AppDeepLink.HomeUrl, holiday.deepLink)
    }

    @Test
    fun widgetSnapshotSkipsExpiredCustomEvents() {
        val snapshot = CountdownCalculator.makeWidgetSnapshot(
            context = context,
            customEvents = listOf(
                CountdownEvent(
                    id = "expired",
                    title = "已过期",
                    targetDate = "2026-06-11"
                ),
                CountdownEvent(
                    id = "today",
                    title = "今天到期",
                    targetDate = "2026-06-12"
                ),
                CountdownEvent(
                    id = "future",
                    title = "未来事件",
                    targetDate = "2026-06-13"
                )
            ),
            now = LocalDate.of(2026, 6, 12)
        )

        val customCards = snapshot.cards.filter { it.eventId != null }
        assertEquals(listOf("today", "future"), customCards.map { it.eventId })
        assertEquals(listOf(0L, 1L), customCards.map { it.days })
    }

    @Test
    fun widgetSnapshotFallsBackToDefaultCardsWhenAllCustomEventsAreExpired() {
        val snapshot = CountdownCalculator.makeWidgetSnapshot(
            context = context,
            customEvents = listOf(
                CountdownEvent(
                    id = "expired",
                    title = "已过期",
                    targetDate = "2026-06-11"
                )
            ),
            now = LocalDate.of(2026, 6, 12)
        )

        assertEquals(2, snapshot.cards.size)
        assertEquals(listOf(null, null), snapshot.cards.map { it.eventId })
    }
}
