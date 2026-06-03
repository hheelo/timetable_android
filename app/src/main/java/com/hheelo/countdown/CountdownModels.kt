package com.hheelo.countdown

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

@Serializable
data class CountdownEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetDate: String,
    val colorHex: String = "#FF6B4A",
    val isPinned: Boolean = false,
    val sortOrder: Int = 0
) {
    companion object {
        fun empty(offsetDays: Long = 30): CountdownEvent {
            return CountdownEvent(
                title = "新的目标",
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
    val eventId: String?
)

data class CountdownSnapshot(
    val generatedAt: LocalDate,
    val cards: List<CountdownCard>
)

object AppDeepLink {
    const val HomeUrl = "timetable://home"

    fun eventUrl(id: String): String = "timetable://event/${id.lowercase()}"

    fun eventIdFrom(uri: android.net.Uri?): String? {
        if (uri?.scheme != "timetable" || uri.host != "event") return null
        return uri.pathSegments.firstOrNull()
    }
}
