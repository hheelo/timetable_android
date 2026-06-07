package com.hheelo.countdown

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedEventId = AppDeepLink.eventIdFrom(intent?.data)
        setContent {
            CountdownTheme {
                CountdownApp(selectedEventId = selectedEventId)
            }
        }
    }
}

@Composable
private fun CountdownTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = countdownColor(CountdownColorHex.Brand),
            secondary = countdownColor(CountdownColorHex.AccentGreen),
            surface = Color.White,
            background = countdownColor(CountdownColorHex.BackgroundStart)
        ),
        content = content
    )
}

@Composable
private fun CountdownApp(selectedEventId: String?) {
    val context = LocalContext.current
    val store = remember { CountdownStore(context) }
    val scope = rememberCoroutineScope()
    var events by remember { mutableStateOf(store.loadCustomEvents()) }
    var snapshot by remember { mutableStateOf(CountdownCalculator.makeSnapshot(store)) }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(events) {
        snapshot = makeCurrentPreviewSnapshot(events)
    }

    Scaffold { paddingValues ->
        LazyColumn(
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

@Composable
private fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "把重要日子放到桌面上",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = countdownColor(CountdownColorHex.TextPrimary)
        )
        Text(
            "默认显示周末、最近节假日和多个自定义目标日，支持桌面小组件与置顶排序。",
            color = countdownColor(CountdownColorHex.TextSecondary),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PreviewCards(cards: List<CountdownCard>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cards.forEach { card ->
            CountdownCardRow(card)
        }
    }
}

@Composable
private fun CountdownCardRow(card: CountdownCard) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.78f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(card.iconName, countdownColor(card.tintHex))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(card.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(card.subtitle, color = countdownColor(CountdownColorHex.TextSecondary), fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(card.days.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("天", color = countdownColor(CountdownColorHex.TextSecondary), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun IconBubble(iconName: String, tint: Color) {
    val icon = when (iconName) {
        "weekend" -> Icons.Filled.WbSunny
        "holiday" -> Icons.Filled.Event
        "pin" -> Icons.Filled.PushPin
        else -> Icons.Filled.CalendarMonth
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun CustomEditorHeader(onAdd: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("自定义倒计时", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("新增")
            }
        }
        Text(
            "支持配置多个重要日期，小组件会优先展示最近的自定义事件；点桌面小组件可直达对应条目。",
            color = countdownColor(CountdownColorHex.TextSecondary),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CustomEventEditor(
    event: CountdownEvent,
    highlighted: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onChange: (CountdownEvent) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val context = LocalContext.current
    val targetDate = LocalDate.parse(event.targetDate)
    val borderColor = if (highlighted) countdownColor(event.colorHex) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.86f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = event.title,
                        onValueChange = { onChange(event.copy(title = it)) },
                        label = { Text("事件名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = event.isPinned,
                            onCheckedChange = { onChange(event.copy(isPinned = it)) }
                        )
                        Icon(Icons.Filled.PushPin, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("置顶显示", fontSize = 14.sp)
                    }
                }
                Column {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = "上移")
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = "下移")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "删除",
                            tint = countdownColor(CountdownColorHex.Danger)
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onChange(event.copy(targetDate = LocalDate.of(year, month + 1, dayOfMonth).toString()))
                        },
                        targetDate.year,
                        targetDate.monthValue - 1,
                        targetDate.dayOfMonth
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("目标日期 ${targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
            }

            ColorSwatchPicker(selectedHex = event.colorHex) { onChange(event.copy(colorHex = it)) }
        }
    }
}

@Composable
private fun ColorSwatchPicker(selectedHex: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CountdownColorHex.eventSwatches.forEach { hex ->
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(countdownColor(hex))
                    .border(if (selectedHex == hex) 3.dp else 0.dp, Color.White, CircleShape)
                    .clickable { onSelect(hex) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedHex == hex) {
                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.72f)
    ) {
        Text(
            "还没有自定义事件，点右上角“新增”来添加你的第一个倒计时。",
            modifier = Modifier.padding(18.dp),
            color = countdownColor(CountdownColorHex.TextSecondary)
        )
    }
}

@Composable
private fun SavePanel(savedMessage: String?, tintHex: String, onSave: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(containerColor = countdownColor(tintHex)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("保存并刷新小组件")
        }
        savedMessage?.let { Text(it, color = countdownColor(CountdownColorHex.Success), fontSize = 13.sp) }
    }
}

@Composable
private fun Footer() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
        Text("节假日说明", fontWeight = FontWeight.SemiBold)
        Text(
            "当前内置了 2026 年大陆法定节假日数据；自定义倒计时支持多条配置。后续年份可在 HolidayCalendar.kt 中继续追加。",
            color = countdownColor(CountdownColorHex.TextSecondary),
            fontSize = 13.sp
        )
    }
}

private fun <T> List<T>.replaced(index: Int, item: T): List<T> {
    return toMutableList().also { it[index] = item }
}

private fun <T> List<T>.removedAt(index: Int): List<T> {
    return toMutableList().also { it.removeAt(index) }
}

private fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    return toMutableList().also { it.swap(from, to) }
}

private fun <T> MutableList<T>.swap(from: Int, to: Int) {
    val item = this[from]
    this[from] = this[to]
    this[to] = item
}
