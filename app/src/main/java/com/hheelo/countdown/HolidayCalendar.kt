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

    private fun holidaysByYear(context: Context): Map<Int, List<HolidayRange>> = mapOf(
        2025 to holidays2025(context),
        2026 to holidays2026(context),
        2027 to holidays2027(context),
        2028 to holidays2028(context),
    )

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
