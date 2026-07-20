package com.hheelo.countdown

import android.content.Context
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

@Serializable
data class CountdownEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetDate: String,
    val colorHex: String = CountdownColorHex.Brand,
    val isPinned: Boolean = false,
    val sortOrder: Int = 0,
    val reminderEnabled: Boolean = false,
    val reminderDaysBefore: Int = 1
) {
    companion object {
        fun empty(context: Context, offsetDays: Long = 30): CountdownEvent {
            return CountdownEvent(
                title = context.getString(R.string.new_goal),
                targetDate = LocalDate.now().plusDays(offsetDays).toString()
            )
        }
    }
}

data class CountdownCard(
    val title: String,
    val subtitle: String,
    val days: Long,
    val iconName: String,
    val tintHex: String,
    val deepLink: String,
    val eventId: String?,
    val isPinned: Boolean = false,
    val status: CountdownCardStatus = CountdownCardStatus.UPCOMING
)

enum class CountdownCardStatus {
    UPCOMING,
    TODAY,
    ONGOING,
    EXPIRED,
    UNAVAILABLE
}

fun CountdownCard.displayValue(): String {
    return if (status == CountdownCardStatus.UNAVAILABLE) "--" else days.toString()
}

fun CountdownCard.accessibilityText(context: Context): String {
    return when (status) {
        CountdownCardStatus.TODAY -> context.getString(R.string.accessibility_today, title)
        CountdownCardStatus.ONGOING -> context.getString(R.string.accessibility_ongoing, title)
        CountdownCardStatus.EXPIRED -> context.getString(R.string.accessibility_days_elapsed, title, days)
        CountdownCardStatus.UNAVAILABLE -> context.getString(R.string.accessibility_unavailable, title, subtitle)
        CountdownCardStatus.UPCOMING -> context.getString(R.string.accessibility_days_remaining, title, days)
    }
}

data class CountdownSnapshot(
    val generatedAt: LocalDate,
    val cards: List<CountdownCard>
)

data class CountdownUiState(
    val events: List<CountdownEvent>,
    val snapshot: CountdownSnapshot,
    val isLoading: Boolean,
    val isSaving: Boolean,
    val statusMessage: String?,
    val statusMessageIsError: Boolean
)

object AppDeepLink {
    const val HomeUrl = "timetable://home"

    fun eventUrl(id: String): String = "timetable://event/${id.lowercase()}"

    fun eventIdFrom(uri: android.net.Uri?): String? {
        if (uri?.scheme != "timetable" || uri.host != "event") return null
        return uri.pathSegments.firstOrNull()
    }
}
