package com.hheelo.countdown

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class CountdownViewModel(application: Application) : AndroidViewModel(application) {
    private val store = CountdownStore(application)

    var events by mutableStateOf<List<CountdownEvent>>(emptyList())
        private set

    var snapshot by mutableStateOf(CountdownSnapshot(LocalDate.now(), CountdownCalculator.makeDefaultCards()))
        private set

    var savedMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) { store.loadCustomEvents() }
            events = loaded
            snapshot = makeCurrentPreviewSnapshot(loaded)
        }
    }

    fun add() {
        val offset = 7L * (events.size + 1)
        updateEvents(events + CountdownEvent.empty(offset))
    }

    fun update(index: Int, updated: CountdownEvent) {
        if (index !in events.indices) return
        updateEvents(events.replaced(index, updated))
    }

    fun delete(index: Int) {
        if (index !in events.indices) return
        updateEvents(events.removedAt(index))
    }

    fun moveUp(index: Int) {
        if (index <= 0 || index >= events.size) return
        updateEvents(events.moved(index, index - 1))
    }

    fun moveDown(index: Int) {
        if (index < 0 || index >= events.lastIndex) return
        updateEvents(events.moved(index, index + 1))
    }

    fun save() {
        val current = events
        savedMessage = "已保存并刷新桌面小组件"
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                store.saveCustomEvents(current)
                CountdownWidget().updateAll(getApplication())
            }
        }
    }

    fun refresh() {
        snapshot = makeCurrentPreviewSnapshot(events)
    }

    private fun updateEvents(updated: List<CountdownEvent>) {
        events = updated
        savedMessage = null
        snapshot = makeCurrentPreviewSnapshot(updated)
    }

    private fun makeCurrentPreviewSnapshot(events: List<CountdownEvent>): CountdownSnapshot {
        val now = LocalDate.now()
        return CountdownSnapshot(
            generatedAt = now,
            cards = CountdownCalculator.makeDefaultCards(now) + events.map { makePreviewCard(it, now) }
        )
    }

    private fun makePreviewCard(event: CountdownEvent, now: LocalDate): CountdownCard {
        val days = CountdownCalculator.daysBetween(now, LocalDate.parse(event.targetDate)).coerceAtLeast(0)
        val title = event.title.trim().ifEmpty { "未命名事件" }
        return CountdownCard(
            title = if (event.isPinned) "置顶 · $title" else title,
            subtitle = if (days == 0L) "今天就是目标日" else "你的自定义倒计时",
            days = days,
            iconName = if (event.isPinned) "pin" else "calendar",
            tintHex = event.colorHex,
            deepLink = AppDeepLink.eventUrl(event.id),
            eventId = event.id,
            isPinned = event.isPinned
        )
    }
}
