package com.hheelo.countdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HolidayCalendarTest {
    @Test
    fun availableYearsIncludesCurrentBundledHolidayData() {
        assertTrue(HolidayCalendar.availableYears().contains(2026))
    }

    @Test
    fun lookupChecksNextYearWhenCurrentYearHasNoBundledData() {
        val result = HolidayCalendar.lookup(LocalDate.of(2025, 12, 31))

        assertEquals(listOf(2025, 2026), result.checkedYears)
        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals("元旦", result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2026, 1, 1), result.upcomingHoliday?.start)
        assertEquals(LocalDate.of(2026, 1, 3), result.upcomingHoliday?.end)
    }

    @Test
    fun lookupReturnsHolidayThatIsCurrentlyInProgress() {
        val result = HolidayCalendar.lookup(LocalDate.of(2026, 2, 18))

        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals("春节", result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2026, 2, 15), result.upcomingHoliday?.start)
        assertEquals(LocalDate.of(2026, 2, 23), result.upcomingHoliday?.end)
    }

    @Test
    fun lookupReportsWhenCurrentYearIsExhaustedAndNextYearIsMissing() {
        val result = HolidayCalendar.lookup(LocalDate.of(2026, 10, 8))

        assertNull(result.upcomingHoliday)
        assertEquals(HolidayLookupStatus.CURRENT_YEAR_EXHAUSTED_NEXT_YEAR_DATA_MISSING, result.status)
        assertEquals(listOf(2026, 2027), result.checkedYears)
        assertEquals(listOf(2026), result.availableYears)
        assertEquals(listOf(2027), result.missingYears)
    }

    @Test
    fun lookupReportsMissingCurrentYearWhenNoCheckedYearHasData() {
        val result = HolidayCalendar.lookup(LocalDate.of(2027, 1, 1))

        assertNull(result.upcomingHoliday)
        assertEquals(HolidayLookupStatus.CURRENT_YEAR_DATA_MISSING, result.status)
        assertEquals(listOf(2027, 2028), result.checkedYears)
        assertEquals(emptyList<Int>(), result.availableYears)
        assertEquals(listOf(2027, 2028), result.missingYears)
    }
}
