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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel

private const val FirstEventItemIndex = 3

@Composable
internal fun CountdownApp(
    selectedEventId: String?,
    selectedEventRequest: Int
) {
    val vm: CountdownViewModel = viewModel()
    val listState = rememberLazyListState()
    val events = vm.events
    val snapshot = vm.snapshot
    val savedMessage = vm.savedMessage

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(selectedEventId, selectedEventRequest, events) {
        val selectedIndex = events.indexOfFirst { it.id == selectedEventId }
        if (selectedIndex >= 0) {
            listState.animateScrollToItem(FirstEventItemIndex + selectedIndex)
        }
    }

    Scaffold { paddingValues ->
        val extraColors = LocalCountdownColors.current
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            extraColors.backgroundStart,
                            extraColors.backgroundMiddle,
                            extraColors.backgroundEnd
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
                    onAdd = { vm.add() }
                )
            }
            itemsIndexed(events, key = { _, event -> event.id }) { index, event ->
                CustomEventEditor(
                    event = event,
                    highlighted = event.id == selectedEventId,
                    canMoveUp = index > 0,
                    canMoveDown = index < events.lastIndex,
                    onChange = { updated -> vm.update(index, updated) },
                    onDelete = { vm.delete(index) },
                    onMoveUp = { vm.moveUp(index) },
                    onMoveDown = { vm.moveDown(index) }
                )
            }
            if (events.isEmpty()) {
                item { EmptyState() }
            }
            item {
                SavePanel(
                    savedMessage = savedMessage,
                    tintHex = events.firstOrNull()?.colorHex ?: CountdownColorHex.Brand,
                    onSave = { vm.save() }
                )
            }
            item { Footer() }
        }
    }
}
