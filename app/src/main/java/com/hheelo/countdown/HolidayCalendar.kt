package com.hheelo.countdown

import android.content.Context
import java.time.LocalDate

data class HolidayRange(
    val name: String,
    val start: LocalDate,
    val end: LocalDate
)

enum class HolidayLookupStatus {
    UPCOMING_FOUND,
    CURRENT_YEAR_DATA_MISSING,
    CURRENT_YEAR_EXHAUSTED_NEXT_YEAR_DATA_MISSING,
    CHECKED_YEARS_DATA_MISSING
}

data class HolidayLookupResult(
    val from: LocalDate,
    val checkedYears: List<Int>,
    val availableYears: List<Int>,
    val missingYears: List<Int>,
    val upcomingHoliday: HolidayRange?,
    val status: HolidayLookupStatus
)

object HolidayCalendar {
    fun upcomingHoliday(context: Context, from: LocalDate = LocalDate.now()): HolidayRange? {
        return lookup(context, from).upcomingHoliday
    }

    fun lookup(context: Context, from: LocalDate = LocalDate.now()): HolidayLookupResult {
        val checkedYears = yearsToCheck(from.year)
        val rangesByYear = checkedYears.associateWith { holidayRanges(context, it) }
        val upcomingHoliday = rangesByYear.values
            .flatten()
            .sortedBy { it.start }
            .firstOrNull { !it.end.isBefore(from) }
        val availableYears = rangesByYear
            .filterValues { it.isNotEmpty() }
            .keys
            .toList()
        val missingYears = checkedYears - availableYears.toSet()

        return HolidayLookupResult(
            from = from,
            checkedYears = checkedYears,
            availableYears = availableYears,
            missingYears = missingYears,
            upcomingHoliday = upcomingHoliday,
            status = statusFor(
                from = from,
                upcomingHoliday = upcomingHoliday,
                availableYears = availableYears,
                missingYears = missingYears
            )
        )
    }

    fun holidayRanges(context: Context, year: Int): List<HolidayRange> {
        return holidaysByYear(context)[year].orEmpty()
    }

    fun availableYears(context: Context): Set<Int> {
        return holidaysByYear(context).keys
    }

    private fun holiday(
        name: String,
        year: Int,
        startMonth: Int,
        startDay: Int,
        endMonth: Int,
        endDay: Int
    ): HolidayRange {
        return HolidayRange(
            name = name,
            start = LocalDate.of(year, startMonth, startDay),
            end = LocalDate.of(year, endMonth, endDay)
        )
    }

    private fun yearsToCheck(currentYear: Int): List<Int> {
        return listOf(currentYear, currentYear + 1)
    }

    private fun statusFor(
        from: LocalDate,
        upcomingHoliday: HolidayRange?,
        availableYears: List<Int>,
        missingYears: List<Int>
    ): HolidayLookupStatus {
        if (upcomingHoliday != null) {
            return HolidayLookupStatus.UPCOMING_FOUND
        }
        if (availableYears.isEmpty()) {
            return if (from.year in missingYears) {
                HolidayLookupStatus.CURRENT_YEAR_DATA_MISSING
            } else {
                HolidayLookupStatus.CHECKED_YEARS_DATA_MISSING
            }
        }
        if (from.year in availableYears && from.year + 1 in missingYears) {
            return HolidayLookupStatus.CURRENT_YEAR_EXHAUSTED_NEXT_YEAR_DATA_MISSING
        }
        return HolidayLookupStatus.CHECKED_YEARS_DATA_MISSING
    }

    /** Last year with official hardcoded data. */
    private const val LAST_HARDCODED_YEAR = 2028

    /** Last year with pre-calculated lunar calendar estimates. */
    private const val LAST_LUNAR_ESTIMATE_YEAR = 2035

    /**
     * Pre-calculated lunar-based holiday dates for 2029-2035.
     * Key: year, Value: Triple(springFestival monthDay, dragonBoat monthDay, midAutumn monthDay)
     */
    private val lunarEstimates: Map<Int, Triple<Pair<Int, Int>, Pair<Int, Int>, Pair<Int, Int>>> = mapOf(
        2029 to Triple(1 to 13, 5 to 28, 9 to 22),
        2030 to Triple(2 to 3, 6 to 17, 10 to 12),
        2031 to Triple(1 to 23, 6 to 6, 10 to 1),
        2032 to Triple(2 to 11, 6 to 24, 9 to 19),
        2033 to Triple(1 to 31, 6 to 13, 9 to 8),
        2034 to Triple(2 to 19, 6 to 3, 9 to 27),
        2035 to Triple(2 to 8, 6 to 22, 9 to 16),
    )

    private fun holidaysByYear(context: Context): Map<Int, List<HolidayRange>> {
        val hardcoded = mapOf(
            2025 to holidays2025(context),
            2026 to holidays2026(context),
            2027 to holidays2027(context),
            2028 to holidays2028(context),
        )
        return HolidayMapWithEstimates(hardcoded, context)
    }

    /**
     * A Map implementation that returns hardcoded data for 2025-2028 and generates
     * estimated holidays on-the-fly for any year beyond that.
     */
    private class HolidayMapWithEstimates(
        private val hardcoded: Map<Int, List<HolidayRange>>,
        private val context: Context
    ) : Map<Int, List<HolidayRange>> by hardcoded {
        override fun get(key: Int): List<HolidayRange>? {
            return hardcoded[key] ?: if (key > LAST_HARDCODED_YEAR) {
                estimateHolidaysForYear(context, key)
            } else {
                null
            }
        }

        override val keys: Set<Int>
            get() = hardcoded.keys

        override fun containsKey(key: Int): Boolean {
            return key in hardcoded || key > LAST_HARDCODED_YEAR
        }
    }

    /**
     * Generates estimated holidays for a year beyond the hardcoded data.
     * Fixed solar holidays use their known dates. Lunar-based holidays use
     * pre-calculated dates for 2029-2035, or the most recent known year's
     * month/day as a rough fallback for years beyond 2035.
     */
    fun estimateHolidaysForYear(context: Context, year: Int): List<HolidayRange> {
        val estimated = context.getString(R.string.holiday_estimated)

        // Lunar-based dates: use pre-calculated if available, otherwise use last known year
        val lunarDates = lunarEstimates[year] ?: lunarEstimates[LAST_LUNAR_ESTIMATE_YEAR]!!

        val springFestival = lunarDates.first
        val dragonBoat = lunarDates.second
        val midAutumn = lunarDates.third

        return listOf(
            // 元旦 - Jan 1 (fixed)
            HolidayRange(
                name = context.getString(R.string.holiday_new_year) + estimated,
                start = LocalDate.of(year, 1, 1),
                end = LocalDate.of(year, 1, 1)
            ),
            // 春节 - lunar new year
            HolidayRange(
                name = context.getString(R.string.holiday_spring_festival) + estimated,
                start = LocalDate.of(year, springFestival.first, springFestival.second),
                end = LocalDate.of(year, springFestival.first, springFestival.second).plusDays(6)
            ),
            // 清明 - Apr 5 (estimate; actual is Apr 4 or 5)
            HolidayRange(
                name = context.getString(R.string.holiday_qingming) + estimated,
                start = LocalDate.of(year, 4, 5),
                end = LocalDate.of(year, 4, 7)
            ),
            // 劳动节 - May 1 (fixed)
            HolidayRange(
                name = context.getString(R.string.holiday_labor_day) + estimated,
                start = LocalDate.of(year, 5, 1),
                end = LocalDate.of(year, 5, 5)
            ),
            // 端午 - lunar 5th month 5th day
            HolidayRange(
                name = context.getString(R.string.holiday_dragon_boat) + estimated,
                start = LocalDate.of(year, dragonBoat.first, dragonBoat.second),
                end = LocalDate.of(year, dragonBoat.first, dragonBoat.second).plusDays(2)
            ),
            // 中秋 - lunar 8th month 15th day
            HolidayRange(
                name = context.getString(R.string.holiday_mid_autumn) + estimated,
                start = LocalDate.of(year, midAutumn.first, midAutumn.second),
                end = LocalDate.of(year, midAutumn.first, midAutumn.second).plusDays(2)
            ),
            // 国庆 - Oct 1 (fixed)
            HolidayRange(
                name = context.getString(R.string.holiday_national_day) + estimated,
                start = LocalDate.of(year, 10, 1),
                end = LocalDate.of(year, 10, 7)
            ),
        )
    }

    private fun holidays2025(context: Context): List<HolidayRange> {
        return listOf(
            holiday(context.getString(R.string.holiday_new_year), 2025, 1, 1, 1, 1),
            holiday(context.getString(R.string.holiday_spring_festival), 2025, 1, 28, 2, 4),
            holiday(context.getString(R.string.holiday_qingming), 2025, 4, 4, 4, 6),
            holiday(context.getString(R.string.holiday_labor_day), 2025, 5, 1, 5, 5),
            holiday(context.getString(R.string.holiday_dragon_boat), 2025, 5, 31, 6, 2),
            holiday(context.getString(R.string.holiday_national_day_mid_autumn), 2025, 10, 1, 10, 8)
        )
    }

    private fun holidays2026(context: Context): List<HolidayRange> {
        return listOf(
            holiday(context.getString(R.string.holiday_new_year), 2026, 1, 1, 1, 3),
            holiday(context.getString(R.string.holiday_spring_festival), 2026, 2, 15, 2, 23),
            holiday(context.getString(R.string.holiday_qingming), 2026, 4, 4, 4, 6),
            holiday(context.getString(R.string.holiday_labor_day), 2026, 5, 1, 5, 5),
            holiday(context.getString(R.string.holiday_dragon_boat), 2026, 6, 19, 6, 21),
            holiday(context.getString(R.string.holiday_mid_autumn), 2026, 9, 25, 9, 27),
            holiday(context.getString(R.string.holiday_national_day), 2026, 10, 1, 10, 7)
        )
    }

    private fun holidays2027(context: Context): List<HolidayRange> {
        return listOf(
            holiday(context.getString(R.string.holiday_new_year), 2027, 1, 1, 1, 3),
            holiday(context.getString(R.string.holiday_spring_festival), 2027, 2, 6, 2, 12),
            holiday(context.getString(R.string.holiday_qingming), 2027, 4, 5, 4, 7),
            holiday(context.getString(R.string.holiday_labor_day), 2027, 5, 1, 5, 5),
            holiday(context.getString(R.string.holiday_dragon_boat), 2027, 6, 19, 6, 21),
            holiday(context.getString(R.string.holiday_mid_autumn), 2027, 9, 25, 9, 27),
            holiday(context.getString(R.string.holiday_national_day), 2027, 10, 1, 10, 7)
        )
    }

    private fun holidays2028(context: Context): List<HolidayRange> {
        return listOf(
            holiday(context.getString(R.string.holiday_new_year), 2028, 1, 1, 1, 3),
            holiday(context.getString(R.string.holiday_spring_festival), 2028, 1, 26, 2, 1),
            holiday(context.getString(R.string.holiday_qingming), 2028, 4, 4, 4, 6),
            holiday(context.getString(R.string.holiday_labor_day), 2028, 5, 1, 5, 5),
            holiday(context.getString(R.string.holiday_dragon_boat), 2028, 6, 8, 6, 10),
            holiday(context.getString(R.string.holiday_national_day_mid_autumn), 2028, 10, 1, 10, 8)
        )
    }
}
