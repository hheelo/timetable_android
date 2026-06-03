package com.hheelo.countdown

import java.time.LocalDate

data class HolidayRange(
    val name: String,
    val start: LocalDate,
    val end: LocalDate
)

object HolidayCalendar {
    fun upcomingHoliday(from: LocalDate = LocalDate.now()): HolidayRange? {
        return holidayRanges(from.year)
            .sortedBy { it.start }
            .firstOrNull { !it.end.isBefore(from) }
    }

    fun holidayRanges(year: Int): List<HolidayRange> {
        return when (year) {
            2026 -> listOf(
                holiday("元旦", year, 1, 1, 1, 3),
                holiday("春节", year, 2, 15, 2, 23),
                holiday("清明", year, 4, 4, 4, 6),
                holiday("劳动节", year, 5, 1, 5, 5),
                holiday("端午", year, 6, 19, 6, 21),
                holiday("中秋", year, 9, 25, 9, 27),
                holiday("国庆", year, 10, 1, 10, 7)
            )
            else -> emptyList()
        }
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
}
