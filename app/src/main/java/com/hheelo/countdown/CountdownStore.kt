package com.hheelo.countdown

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

class CountdownStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("countdown_store", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun loadCustomEvents(): List<CountdownEvent> {
        val encoded = preferences.getString(CustomEventsKey, null)
        val decoded = encoded?.let {
            runCatching { json.decodeFromString<List<CountdownEvent>>(it) }.getOrNull()
        }

        if (!decoded.isNullOrEmpty()) return normalized(decoded)

        return normalized(
            listOf(
                CountdownEvent(
                    title = "产品上线",
                    targetDate = LocalDate.now().plusDays(30).toString(),
                    colorHex = "#FF6B4A"
                )
            )
        )
    }

    fun saveCustomEvents(events: List<CountdownEvent>) {
        preferences.edit()
            .putString(CustomEventsKey, json.encodeToString(normalized(events)))
            .apply()
    }

    private fun normalized(events: List<CountdownEvent>): List<CountdownEvent> {
        return events
            .mapIndexed { index, event ->
                event.copy(
                    title = event.title.trim().ifEmpty { "未命名事件" },
                    sortOrder = index
                )
            }
            .sortedWith(
                compareByDescending<CountdownEvent> { it.isPinned }
                    .thenBy { it.sortOrder }
                    .thenBy { LocalDate.parse(it.targetDate) }
            )
    }

    companion object {
        private const val CustomEventsKey = "customCountdownEvents"
    }
}
