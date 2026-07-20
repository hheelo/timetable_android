package com.hheelo.countdown

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object CountdownCalculator {
    fun makeSnapshot(context: Context, store: CountdownStore, now: LocalDate = LocalDate.now()): CountdownSnapshot {
        return makeSnapshot(
            context = context,
            customEvents = store.loadCustomEvents(),
            now = now,
            includeExpiredCustomEvents = true
        )
    }

    fun makeWidgetSnapshot(context: Context, store: CountdownStore, now: LocalDate = LocalDate.now()): CountdownSnapshot {
        return makeWidgetSnapshot(
            context = context,
            customEvents = store.loadCustomEvents(),
            now = now
        )
    }

    fun makeWidgetSnapshot(
        context: Context,
        customEvents: List<CountdownEvent>,
        now: LocalDate = LocalDate.now()
    ): CountdownSnapshot {
        return makeSnapshot(
            context = context,
            customEvents = customEvents,
            now = now,
            includeExpiredCustomEvents = false
        )
    }

    fun makePreviewSnapshot(
        context: Context,
        customEvents: List<CountdownEvent>,
        now: LocalDate = LocalDate.now()
    ): CountdownSnapshot {
        return makeSnapshot(
            context = context,
            customEvents = customEvents,
            now = now,
            includeExpiredCustomEvents = true
        )
    }

    private fun makeSnapshot(
        context: Context,
        customEvents: List<CountdownEvent>,
        now: LocalDate,
        includeExpiredCustomEvents: Boolean
    ): CountdownSnapshot {
        val customCards = customEvents
            .mapNotNull { event ->
                val target = targetDateFor(event) ?: return@mapNotNull null
                if (!includeExpiredCustomEvents && target.isBefore(now)) return@mapNotNull null
                makeCustomCard(context, event, target, now)
            }
        return CountdownSnapshot(
            generatedAt = now,
            cards = makeDefaultCards(context, now) + customCards
        )
    }

    fun makeDefaultCards(context: Context, now: LocalDate = LocalDate.now()): List<CountdownCard> {
        return listOf(makeWeekendCard(context, now), makeHolidayCard(context, now))
    }

    fun daysBetween(from: LocalDate, to: LocalDate): Long {
        return ChronoUnit.DAYS.between(from, to)
    }

    private fun makeWeekendCard(context: Context, now: LocalDate): CountdownCard {
        val target = nextWeekendStart(now)
        val days = daysBetween(now, target).coerceAtLeast(0)
        return CountdownCard(
            title = context.getString(R.string.weekend_title),
            subtitle = if (days == 0L) context.getString(R.string.weekend_today) else context.getString(R.string.weekend_countdown),
            days = days,
            iconName = "weekend",
            tintHex = CountdownColorHex.Weekend,
            deepLink = AppDeepLink.HomeUrl,
            eventId = null,
            status = if (days == 0L) CountdownCardStatus.TODAY else CountdownCardStatus.UPCOMING
        )
    }

    private fun makeHolidayCard(context: Context, now: LocalDate): CountdownCard {
        val lookup = HolidayCalendar.lookup(context, now)
        val holiday = lookup.upcomingHoliday
        if (lookup.status == HolidayLookupStatus.UPCOMING_FOUND && holiday != null) {
            val days = daysBetween(now, holiday.start).coerceAtLeast(0)
            return CountdownCard(
                title = holiday.name,
                subtitle = if (!now.isBefore(holiday.start)) context.getString(R.string.holiday_ongoing) else context.getString(R.string.holiday_countdown, holiday.name),
                days = days,
                iconName = "holiday",
                tintHex = CountdownColorHex.Holiday,
                deepLink = AppDeepLink.HomeUrl,
                eventId = null,
                status = if (now.isBefore(holiday.start)) {
                    CountdownCardStatus.UPCOMING
                } else {
                    CountdownCardStatus.ONGOING
                }
            )
        }

        val subtitle = when (lookup.status) {
            HolidayLookupStatus.CURRENT_YEAR_EXHAUSTED_NEXT_YEAR_DATA_MISSING ->
                context.getString(R.string.holiday_year_exhausted, now.year + 1)
            else -> context.getString(R.string.holiday_no_data, now.year)
        }
        return CountdownCard(
            title = context.getString(R.string.holiday_title),
            subtitle = subtitle,
            days = 0,
            iconName = "holiday",
            tintHex = CountdownColorHex.Holiday,
            deepLink = AppDeepLink.HomeUrl,
            eventId = null,
            status = CountdownCardStatus.UNAVAILABLE
        )
    }

    private fun makeCustomCard(
        context: Context,
        event: CountdownEvent,
        target: LocalDate,
        now: LocalDate
    ): CountdownCard {
        val signedDays = daysBetween(now, target)
        val status = when {
            signedDays < 0 -> CountdownCardStatus.EXPIRED
            signedDays == 0L -> CountdownCardStatus.TODAY
            else -> CountdownCardStatus.UPCOMING
        }
        val days = kotlin.math.abs(signedDays)
        val title = event.title.trim().ifEmpty { context.getString(R.string.unnamed_event) }
        return CountdownCard(
            title = if (event.isPinned) context.getString(R.string.pinned_prefix, title) else title,
            subtitle = when (status) {
                CountdownCardStatus.EXPIRED -> context.getString(R.string.custom_event_expired)
                CountdownCardStatus.TODAY -> context.getString(R.string.custom_event_today)
                else -> context.getString(R.string.custom_event_countdown)
            },
            days = days,
            iconName = if (event.isPinned) "pin" else "calendar",
            tintHex = event.colorHex,
            deepLink = AppDeepLink.eventUrl(event.id),
            eventId = event.id,
            isPinned = event.isPinned,
            status = status
        )
    }

    private fun targetDateFor(event: CountdownEvent): LocalDate? {
        return runCatching { LocalDate.parse(event.targetDate) }.getOrNull()
    }

    private fun nextWeekendStart(now: LocalDate): LocalDate {
        return when (now.dayOfWeek) {
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> now
            else -> now.plusDays((DayOfWeek.SATURDAY.value - now.dayOfWeek.value).toLong())
        }
    }
}
