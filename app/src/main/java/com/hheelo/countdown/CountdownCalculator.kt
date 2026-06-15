package com.hheelo.countdown

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object CountdownCalculator {
    fun makeSnapshot(store: CountdownStore, now: LocalDate = LocalDate.now()): CountdownSnapshot {
        return makeSnapshot(
            customEvents = store.loadCustomEvents(),
            now = now,
            includeExpiredCustomEvents = true
        )
    }

    fun makeWidgetSnapshot(store: CountdownStore, now: LocalDate = LocalDate.now()): CountdownSnapshot {
        return makeWidgetSnapshot(
            customEvents = store.loadCustomEvents(),
            now = now
        )
    }

    fun makeWidgetSnapshot(
        customEvents: List<CountdownEvent>,
        now: LocalDate = LocalDate.now()
    ): CountdownSnapshot {
        return makeSnapshot(
            customEvents = customEvents,
            now = now,
            includeExpiredCustomEvents = false
        )
    }

    private fun makeSnapshot(
        customEvents: List<CountdownEvent>,
        now: LocalDate,
        includeExpiredCustomEvents: Boolean
    ): CountdownSnapshot {
        val customCards = customEvents
            .filter { includeExpiredCustomEvents || !targetDateFor(it).isBefore(now) }
            .map { makeCustomCard(it, now) }
        return CountdownSnapshot(
            generatedAt = now,
            cards = makeDefaultCards(now) + customCards
        )
    }

    fun makeDefaultCards(now: LocalDate = LocalDate.now()): List<CountdownCard> {
        return listOf(makeWeekendCard(now), makeHolidayCard(now))
    }

    fun daysBetween(from: LocalDate, to: LocalDate): Long {
        return ChronoUnit.DAYS.between(from, to)
    }

    private fun makeWeekendCard(now: LocalDate): CountdownCard {
        val target = nextWeekendStart(now)
        val days = daysBetween(now, target).coerceAtLeast(0)
        return CountdownCard(
            title = "周末",
            subtitle = if (days == 0L) "今天就是周末" else "距离周末还有",
            days = days,
            iconName = "weekend",
            tintHex = CountdownColorHex.Weekend,
            deepLink = AppDeepLink.HomeUrl,
            eventId = null
        )
    }

    private fun makeHolidayCard(now: LocalDate): CountdownCard {
        val lookup = HolidayCalendar.lookup(now)
        val holiday = lookup.upcomingHoliday
        if (lookup.status == HolidayLookupStatus.UPCOMING_FOUND && holiday != null) {
            val days = daysBetween(now, holiday.start).coerceAtLeast(0)
            return CountdownCard(
                title = holiday.name,
                subtitle = if (!now.isBefore(holiday.start)) "正在放假中" else "距离${holiday.name}还有",
                days = days,
                iconName = "holiday",
                tintHex = CountdownColorHex.Holiday,
                deepLink = AppDeepLink.HomeUrl,
                eventId = null
            )
        }

        val subtitle = when (lookup.status) {
            HolidayLookupStatus.CURRENT_YEAR_EXHAUSTED_NEXT_YEAR_DATA_MISSING ->
                "今年假期已过，${now.year + 1} 年数据待更新"
            else -> "暂无 ${now.year} 年节假日数据"
        }
        return CountdownCard(
            title = "节假日",
            subtitle = subtitle,
            days = 0,
            iconName = "holiday",
            tintHex = CountdownColorHex.Holiday,
            deepLink = AppDeepLink.HomeUrl,
            eventId = null
        )
    }

    private fun makeCustomCard(event: CountdownEvent, now: LocalDate): CountdownCard {
        val target = targetDateFor(event)
        val days = daysBetween(now, target).coerceAtLeast(0)
        return CountdownCard(
            title = if (event.isPinned) "置顶 · ${event.title}" else event.title,
            subtitle = if (days == 0L) "今天就是目标日" else "你的自定义倒计时",
            days = days,
            iconName = if (event.isPinned) "pin" else "calendar",
            tintHex = event.colorHex,
            deepLink = AppDeepLink.eventUrl(event.id),
            eventId = event.id,
            isPinned = event.isPinned
        )
    }

    private fun targetDateFor(event: CountdownEvent): LocalDate {
        return LocalDate.parse(event.targetDate)
    }

    private fun nextWeekendStart(now: LocalDate): LocalDate {
        return when (now.dayOfWeek) {
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> now
            else -> now.plusDays((DayOfWeek.SATURDAY.value - now.dayOfWeek.value).toLong())
        }
    }
}
