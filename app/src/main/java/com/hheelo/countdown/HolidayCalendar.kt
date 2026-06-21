package com.hheelo.countdown

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
    fun upcomingHoliday(from: LocalDate = LocalDate.now()): HolidayRange? {
        return lookup(from).upcomingHoliday
    }

    fun lookup(from: LocalDate = LocalDate.now()): HolidayLookupResult {
        val checkedYears = yearsToCheck(from.year)
        val rangesByYear = checkedYears.associateWith(::holidayRanges)
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

    fun holidayRanges(year: Int): List<HolidayRange> {
        return HOLIDAYS_BY_YEAR[year].orEmpty()
    }

    fun availableYears(): Set<Int> {
        return HOLIDAYS_BY_YEAR.keys
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

    private val HOLIDAYS_BY_YEAR: Map<Int, List<HolidayRange>> = mapOf(
        2025 to holidays2025(),
        2026 to holidays2026(),
        2027 to holidays2027(),
        2028 to holidays2028(),
        // Holiday data covers 2025-2028.
        // Add future confirmed mainland China statutory holiday data here.
    )

    private fun holidays2025(): List<HolidayRange> {
        return listOf(
            holiday("元旦", 2025, 1, 1, 1, 1),
            holiday("春节", 2025, 1, 28, 2, 4),
            holiday("清明", 2025, 4, 4, 4, 6),
            holiday("劳动节", 2025, 5, 1, 5, 5),
            holiday("端午", 2025, 5, 31, 6, 2),
            holiday("国庆+中秋", 2025, 10, 1, 10, 8)
        )
    }

    private fun holidays2026(): List<HolidayRange> {
        return listOf(
            holiday("元旦", 2026, 1, 1, 1, 3),
            holiday("春节", 2026, 2, 15, 2, 23),
            holiday("清明", 2026, 4, 4, 4, 6),
            holiday("劳动节", 2026, 5, 1, 5, 5),
            holiday("端午", 2026, 6, 19, 6, 21),
            holiday("中秋", 2026, 9, 25, 9, 27),
            holiday("国庆", 2026, 10, 1, 10, 7)
        )
    }

    private fun holidays2027(): List<HolidayRange> {
        return listOf(
            holiday("元旦", 2027, 1, 1, 1, 3),
            holiday("春节", 2027, 2, 6, 2, 12),
            holiday("清明", 2027, 4, 5, 4, 7),
            holiday("劳动节", 2027, 5, 1, 5, 5),
            holiday("端午", 2027, 6, 19, 6, 21),
            holiday("中秋", 2027, 9, 25, 9, 27),
            holiday("国庆", 2027, 10, 1, 10, 7)
        )
    }

    private fun holidays2028(): List<HolidayRange> {
        return listOf(
            holiday("元旦", 2028, 1, 1, 1, 3),
            holiday("春节", 2028, 1, 26, 2, 1),
            holiday("清明", 2028, 4, 4, 4, 6),
            holiday("劳动节", 2028, 5, 1, 5, 5),
            holiday("端午", 2028, 6, 8, 6, 10),
            holiday("国庆+中秋", 2028, 10, 1, 10, 8)
        )
    }
}
