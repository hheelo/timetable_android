package com.hheelo.countdown

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class HolidayCalendarTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun availableYearsIncludesAllBundledHolidayData() {
        val years = HolidayCalendar.availableYears(context)
        assertTrue(years.contains(2025))
        assertTrue(years.contains(2026))
        assertTrue(years.contains(2027))
        assertTrue(years.contains(2028))
    }

    @Test
    fun lookupFindsNextYearHolidayWhenCurrentYearExhausted() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2025, 12, 31))

        assertEquals(listOf(2025, 2026), result.checkedYears)
        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals(context.getString(R.string.holiday_new_year), result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2026, 1, 1), result.upcomingHoliday?.start)
        assertEquals(LocalDate.of(2026, 1, 3), result.upcomingHoliday?.end)
    }

    @Test
    fun lookupReturnsHolidayThatIsCurrentlyInProgress() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2026, 2, 18))

        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals(context.getString(R.string.holiday_spring_festival), result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2026, 2, 15), result.upcomingHoliday?.start)
        assertEquals(LocalDate.of(2026, 2, 23), result.upcomingHoliday?.end)
    }

    @Test
    fun lookupReportsWhenCurrentYearIsExhaustedAndNextYearIsMissing() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2028, 10, 9))

        assertNull(result.upcomingHoliday)
        assertEquals(HolidayLookupStatus.CURRENT_YEAR_EXHAUSTED_NEXT_YEAR_DATA_MISSING, result.status)
        assertEquals(listOf(2028, 2029), result.checkedYears)
        assertEquals(listOf(2028), result.availableYears)
        assertEquals(listOf(2029), result.missingYears)
    }

    @Test
    fun lookupReportsMissingCurrentYearWhenNoCheckedYearHasData() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2029, 1, 1))

        assertNull(result.upcomingHoliday)
        assertEquals(HolidayLookupStatus.CURRENT_YEAR_DATA_MISSING, result.status)
        assertEquals(listOf(2029, 2030), result.checkedYears)
        assertEquals(emptyList<Int>(), result.availableYears)
        assertEquals(listOf(2029, 2030), result.missingYears)
    }

    // --- 2025 data tests ---

    @Test
    fun lookup2025NewYear() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2025, 1, 1))

        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals(context.getString(R.string.holiday_new_year), result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2025, 1, 1), result.upcomingHoliday?.start)
        assertEquals(LocalDate.of(2025, 1, 1), result.upcomingHoliday?.end)
    }

    @Test
    fun lookup2025SpringFestival() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2025, 1, 20))

        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals(context.getString(R.string.holiday_spring_festival), result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2025, 1, 28), result.upcomingHoliday?.start)
        assertEquals(LocalDate.of(2025, 2, 4), result.upcomingHoliday?.end)
    }

    @Test
    fun lookup2025NationalDayMergedWithMidAutumn() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2025, 9, 30))

        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals(context.getString(R.string.holiday_national_day_mid_autumn), result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2025, 10, 1), result.upcomingHoliday?.start)
        assertEquals(LocalDate.of(2025, 10, 8), result.upcomingHoliday?.end)
    }

    // --- 2026 data tests (existing coverage) ---

    @Test
    fun lookup2026SpringFestivalDuringHoliday() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2026, 2, 20))

        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals(context.getString(R.string.holiday_spring_festival), result.upcomingHoliday?.name)
    }

    // --- 2027 data tests ---

    @Test
    fun lookup2027SpringFestival() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2027, 1, 15))

        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals(context.getString(R.string.holiday_spring_festival), result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2027, 2, 6), result.upcomingHoliday?.start)
        assertEquals(LocalDate.of(2027, 2, 12), result.upcomingHoliday?.end)
    }

    @Test
    fun lookup2027NationalDay() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2027, 9, 28))

        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals(context.getString(R.string.holiday_national_day), result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2027, 10, 1), result.upcomingHoliday?.start)
        assertEquals(LocalDate.of(2027, 10, 7), result.upcomingHoliday?.end)
    }

    // --- 2028 data tests ---

    @Test
    fun lookup2028SpringFestival() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2028, 1, 10))

        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals(context.getString(R.string.holiday_spring_festival), result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2028, 1, 26), result.upcomingHoliday?.start)
        assertEquals(LocalDate.of(2028, 2, 1), result.upcomingHoliday?.end)
    }

    @Test
    fun lookup2028NationalDayMergedWithMidAutumn() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2028, 10, 3))

        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals(context.getString(R.string.holiday_national_day_mid_autumn), result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2028, 10, 1), result.upcomingHoliday?.start)
        assertEquals(LocalDate.of(2028, 10, 8), result.upcomingHoliday?.end)
    }

    // --- Cross-year boundary tests ---

    @Test
    fun lookupCrossesYearBoundaryFrom2026To2027() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2026, 10, 8))

        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals(context.getString(R.string.holiday_new_year), result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2027, 1, 1), result.upcomingHoliday?.start)
    }

    @Test
    fun lookupCrossesYearBoundaryFrom2027To2028() {
        val result = HolidayCalendar.lookup(context, LocalDate.of(2027, 10, 8))

        assertEquals(HolidayLookupStatus.UPCOMING_FOUND, result.status)
        assertEquals(context.getString(R.string.holiday_new_year), result.upcomingHoliday?.name)
        assertEquals(LocalDate.of(2028, 1, 1), result.upcomingHoliday?.start)
    }

    @Test
    fun holidayCountPerYear() {
        assertEquals(6, HolidayCalendar.holidayRanges(context,2025).size)
        assertEquals(7, HolidayCalendar.holidayRanges(context,2026).size)
        assertEquals(7, HolidayCalendar.holidayRanges(context,2027).size)
        assertEquals(6, HolidayCalendar.holidayRanges(context,2028).size)
    }
}
