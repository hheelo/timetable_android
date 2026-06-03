package com.hheelo.countdown

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object CountdownCalculator {
    fun makeSnapshot(store: CountdownStore, now: LocalDate = LocalDate.now()): CountdownSnapshot {
        val customCards = store.loadCustomEvents().map { makeCustomCard(it, now) }
        return CountdownSnapshot(
            generatedAt = now,
            cards = listOf(makeWeekendCard(now), makeHolidayCard(now)) + customCards
        )
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
            tintHex = "#FDBA74",
            deepLink = AppDeepLink.HomeUrl,
            eventId = null
        )
    }

    private fun makeHolidayCard(now: LocalDate): CountdownCard {
        val holiday = HolidayCalendar.upcomingHoliday(now)
        if (holiday == null) {
            return CountdownCard(
                title = "节假日",
                subtitle = "请补充节假日数据",
                days = 0,
                iconName = "holiday",
                tintHex = "#34D399",
                deepLink = AppDeepLink.HomeUrl,
                eventId = null
            )
        }

        val days = daysBetween(now, holiday.start).coerceAtLeast(0)
        return CountdownCard(
            title = holiday.name,
            subtitle = if (!now.isBefore(holiday.start)) "正在放假中" else "距离${holiday.name}还有",
            days = days,
            iconName = "holiday",
            tintHex = "#34D399",
            deepLink = AppDeepLink.HomeUrl,
            eventId = null
        )
    }

    private fun makeCustomCard(event: CountdownEvent, now: LocalDate): CountdownCard {
        val target = LocalDate.parse(event.targetDate)
        val days = daysBetween(now, target).coerceAtLeast(0)
        return CountdownCard(
            title = if (event.isPinned) "置顶 · ${event.title}" else event.title,
            subtitle = if (days == 0L) "今天就是目标日" else "你的自定义倒计时",
            days = days,
            iconName = if (event.isPinned) "pin" else "calendar",
            tintHex = event.colorHex,
            deepLink = AppDeepLink.eventUrl(event.id),
            eventId = event.id
        )
    }

    private fun nextWeekendStart(now: LocalDate): LocalDate {
        return when (now.dayOfWeek) {
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> now
            else -> now.plusDays((DayOfWeek.SATURDAY.value - now.dayOfWeek.value).toLong())
        }
    }
}
