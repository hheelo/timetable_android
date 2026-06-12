package com.hheelo.countdown

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CountdownCalculatorTest {
    @Test
    fun daysBetweenAllowsFutureAndPastTargets() {
        val from = LocalDate.of(2026, 6, 12)

        assertEquals(2, CountdownCalculator.daysBetween(from, LocalDate.of(2026, 6, 14)))
        assertEquals(-2, CountdownCalculator.daysBetween(from, LocalDate.of(2026, 6, 10)))
    }

    @Test
    fun defaultCardsCountDownToNextSaturdayOnWeekdays() {
        val cards = CountdownCalculator.makeDefaultCards(LocalDate.of(2026, 6, 12))

        val weekend = cards.first()
        assertEquals("周末", weekend.title)
        assertEquals("距离周末还有", weekend.subtitle)
        assertEquals(1, weekend.days)
        assertEquals("weekend", weekend.iconName)
        assertEquals(AppDeepLink.HomeUrl, weekend.deepLink)
    }

    @Test
    fun weekendCardIsDueTodayOnSaturday() {
        val weekend = CountdownCalculator.makeDefaultCards(LocalDate.of(2026, 6, 13)).first()

        assertEquals("今天就是周末", weekend.subtitle)
        assertEquals(0, weekend.days)
    }

    @Test
    fun holidayCardShowsActiveHolidayWhenTodayIsInHolidayRange() {
        val holiday = CountdownCalculator.makeDefaultCards(LocalDate.of(2026, 10, 5))[1]

        assertEquals("国庆", holiday.title)
        assertEquals("正在放假中", holiday.subtitle)
        assertEquals(0, holiday.days)
        assertEquals("holiday", holiday.iconName)
        assertEquals(AppDeepLink.HomeUrl, holiday.deepLink)
    }
}
