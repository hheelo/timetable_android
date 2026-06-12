package com.hheelo.countdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

private const val FirstEventItemIndex = 3

@Composable
internal fun CountdownApp(
    selectedEventId: String?,
    selectedEventRequest: Int
) {
    val context = LocalContext.current
    val store = remember { CountdownStore(context) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var events by remember { mutableStateOf(store.loadCustomEvents()) }
    var snapshot by remember { mutableStateOf(CountdownCalculator.makeSnapshot(store)) }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(events) {
        snapshot = makeCurrentPreviewSnapshot(events)
    }

    LaunchedEffect(selectedEventId, selectedEventRequest, events) {
        val selectedIndex = events.indexOfFirst { it.id == selectedEventId }
        if (selectedIndex >= 0) {
            listState.animateScrollToItem(FirstEventItemIndex + selectedIndex)
        }
    }

    Scaffold { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            countdownColor(CountdownColorHex.BackgroundStart),
                            countdownColor(CountdownColorHex.BackgroundMiddle),
                            countdownColor(CountdownColorHex.BackgroundEnd)
                        )
                    )
                )
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { Header() }
            item { PreviewCards(snapshot.cards) }
            item {
                CustomEditorHeader(
                    onAdd = {
                        val offset = 7L * (events.size + 1)
                        events = events + CountdownEvent.empty(offset)
                        savedMessage = null
                    }
                )
            }
            itemsIndexed(events, key = { _, event -> event.id }) { index, event ->
                CustomEventEditor(
                    event = event,
                    highlighted = event.id == selectedEventId,
                    canMoveUp = index > 0,
                    canMoveDown = index < events.lastIndex,
                    onChange = { updated ->
                        events = events.replaced(index, updated)
                        savedMessage = null
                    },
                    onDelete = {
                        events = events.removedAt(index)
                        savedMessage = null
                    },
                    onMoveUp = {
                        events = events.moved(index, index - 1)
                        savedMessage = null
                    },
                    onMoveDown = {
                        events = events.moved(index, index + 1)
                        savedMessage = null
                    }
                )
            }
            if (events.isEmpty()) {
                item { EmptyState() }
            }
            item {
                SavePanel(
                    savedMessage = savedMessage,
                    tintHex = events.firstOrNull()?.colorHex ?: CountdownColorHex.Brand,
                    onSave = {
                        store.saveCustomEvents(events)
                        events = store.loadCustomEvents()
                        snapshot = CountdownCalculator.makeSnapshot(store)
                        savedMessage = "已保存并刷新桌面小组件"
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                CountdownWidget().updateAll(context)
                            }
                        }
                    }
                )
            }
            item { Footer() }
        }
    }
}

private fun makePreviewCard(event: CountdownEvent, now: LocalDate = LocalDate.now()): CountdownCard {
    val days = CountdownCalculator.daysBetween(now, LocalDate.parse(event.targetDate)).coerceAtLeast(0)
    val title = event.title.trim().ifEmpty { "未命名事件" }
    return CountdownCard(
        title = if (event.isPinned) "置顶 · $title" else title,
        subtitle = if (days == 0L) "今天就是目标日" else "你的自定义倒计时",
        days = days,
        iconName = if (event.isPinned) "pin" else "calendar",
        tintHex = event.colorHex,
        deepLink = AppDeepLink.eventUrl(event.id),
        eventId = event.id
    )
}

private fun makeCurrentPreviewSnapshot(events: List<CountdownEvent>): CountdownSnapshot {
    val now = LocalDate.now()
    return CountdownSnapshot(
        generatedAt = now,
        cards = CountdownCalculator.makeDefaultCards(now) + events.map { makePreviewCard(it, now) }
    )
}
