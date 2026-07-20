package com.hheelo.countdown

import android.content.Context
import android.graphics.Color
import com.hheelo.countdown.logging.AppLog
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import java.time.Instant
import java.time.LocalDate

/**
 * 自定义事件的持久化读写(基于 SharedPreferences）。
 *
 * 设计约定，避免被误当成可优化的冗余：
 * - 本类每次按需新建（ViewModel 初始化一次、小组件每次 provideGlance 一次），
 *   实例不复用，因此「加内存缓存省去重复 load」在当前架构下没有收益。
 * - [saveCustomEvents] 与 [loadCustomEvents] 各调用一次 [normalized]，职责不同、并非重复：
 *   写入侧负责落盘前清洗（trim、补默认色、重排 sortOrder)；
 *   读取侧负责防御外部篡改 / 旧版本数据,并据此触发损坏备份。
 *   删除任一侧都会降低健壮性。
 */
class CountdownStore(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences("countdown_store", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun loadCustomEvents(): List<CountdownEvent> {
        if (!preferences.contains(CustomEventsKey)) {
            AppLog.i(TAG, "无已存事件，返回默认事件")
            return defaultCustomEvents()
        }

        val encoded = runCatching { preferences.getString(CustomEventsKey, null) }
            .onFailure {
                AppLog.e(TAG, "读取已存事件失败", it)
                backupCorruptEvents(null, "stored value is not a string: ${it.safeMessage()}")
            }
            .getOrNull()

        if (encoded.isNullOrBlank()) {
            AppLog.w(TAG, "已存事件为空，已备份并返回空列表")
            backupCorruptEvents(encoded, "stored value is empty")
            return emptyList()
        }

        val decoded = decodeStoredEvents(encoded)
        val normalized = normalized(decoded.events)
        if (decoded.hasInvalidJson || decoded.hasInvalidElements || normalized.hasInvalidFields) {
            val reason = corruptionReason(decoded, normalized)
            AppLog.w(TAG, "检测到损坏数据，已备份。原因: $reason")
            backupCorruptEvents(encoded, reason)
        }

        AppLog.i(TAG, "已加载 ${normalized.events.size} 个事件")
        return normalized.events
    }

    fun saveCustomEvents(events: List<CountdownEvent>) {
        runCatching {
            val saved = preferences.edit()
                .putString(CustomEventsKey, json.encodeToString(normalized(events).events))
                .commit()
            check(saved) { "SharedPreferences commit returned false" }
        }.onSuccess {
            AppLog.i(TAG, "已保存 ${events.size} 个事件")
        }.onFailure {
            AppLog.e(TAG, "保存事件失败", it)
            throw it
        }
    }

    private fun defaultCustomEvents(): List<CountdownEvent> {
        return normalized(
            listOf(
                CountdownEvent(
                    title = appContext.getString(R.string.default_event_title),
                    targetDate = LocalDate.now().plusDays(30).toString(),
                    colorHex = CountdownColorHex.Brand
                )
            )
        ).events
    }

    private fun decodeStoredEvents(encoded: String): DecodedEvents {
        val array = runCatching { json.decodeFromString<JsonArray>(encoded) }
            .getOrElse {
                return DecodedEvents(
                    events = emptyList(),
                    hasInvalidJson = true,
                    hasInvalidElements = false,
                    reason = "json parse failed: ${it.safeMessage()}"
                )
            }

        var hasInvalidElements = false
        val events = array.mapNotNull { item ->
            runCatching { json.decodeFromJsonElement<CountdownEvent>(item) }
                .onFailure { hasInvalidElements = true }
                .getOrNull()
        }

        return DecodedEvents(
            events = events,
            hasInvalidJson = false,
            hasInvalidElements = hasInvalidElements,
            reason = if (hasInvalidElements) "one or more event entries could not be decoded" else null
        )
    }

    private fun normalized(events: List<CountdownEvent>): NormalizedEvents {
        var hasInvalidFields = false
        val normalizedEvents = events
            .mapIndexed { index, event ->
                val targetDate = runCatching { LocalDate.parse(event.targetDate) }.getOrNull()
                if (targetDate == null) {
                    hasInvalidFields = true
                    return@mapIndexed null
                }

                val colorHex = sanitizedColorHex(event.colorHex)
                if (colorHex != event.colorHex) hasInvalidFields = true
                val reminderDaysBefore = event.reminderDaysBefore
                    .takeIf { it in SupportedReminderDays }
                    ?: 1.also { hasInvalidFields = true }

                ValidatedEvent(
                    event = event.copy(
                        title = event.title.trim().ifEmpty { appContext.getString(R.string.unnamed_event) },
                        targetDate = targetDate.toString(),
                        colorHex = colorHex,
                        sortOrder = index,
                        reminderDaysBefore = reminderDaysBefore
                    )
                )
            }
            .filterNotNull()
            .sortedWith(
                compareByDescending<ValidatedEvent> { it.event.isPinned }
                    .thenBy { if (it.event.isPinned) it.event.sortOrder else Int.MAX_VALUE }
                    .thenBy { if (!it.event.isPinned) it.event.sortOrder else Int.MAX_VALUE }
            )
            .mapIndexed { index, validated -> validated.event.copy(sortOrder = index) }

        return NormalizedEvents(normalizedEvents, hasInvalidFields)
    }

    private fun sanitizedColorHex(colorHex: String): String {
        val trimmed = colorHex.trim()
        return if (runCatching { Color.parseColor(trimmed) }.isSuccess) {
            trimmed
        } else {
            CountdownColorHex.Brand
        }
    }

    private fun backupCorruptEvents(encoded: String?, reason: String) {
        val editor = preferences.edit()
            .putString(CorruptEventsReasonKey, reason)
            .putString(CorruptEventsBackedUpAtKey, Instant.now().toString())

        if (encoded != null) {
            editor.putString(CorruptEventsBackupKey, encoded)
        }

        editor.commit()
    }

    private fun corruptionReason(decoded: DecodedEvents, normalized: NormalizedEvents): String {
        val reasons = listOfNotNull(
            decoded.reason,
            if (normalized.hasInvalidFields) "one or more event fields were invalid" else null
        )
        return reasons.joinToString("; ").ifEmpty { "stored events required recovery" }
    }

    private fun Throwable.safeMessage(): String {
        return "${this::class.java.simpleName}${message?.let { ": $it" }.orEmpty()}"
    }

    private data class DecodedEvents(
        val events: List<CountdownEvent>,
        val hasInvalidJson: Boolean,
        val hasInvalidElements: Boolean,
        val reason: String?
    )

    private data class NormalizedEvents(
        val events: List<CountdownEvent>,
        val hasInvalidFields: Boolean
    )

    private data class ValidatedEvent(
        val event: CountdownEvent
    )

    companion object {
        private const val TAG = "CountdownStore"
        private const val CustomEventsKey = "customCountdownEvents"
        private const val CorruptEventsBackupKey = "customCountdownEvents.corruptBackup"
        private const val CorruptEventsReasonKey = "customCountdownEvents.corruptReason"
        private const val CorruptEventsBackedUpAtKey = "customCountdownEvents.corruptBackedUpAt"
        private val SupportedReminderDays = setOf(0, 1, 3, 7)
    }
}
