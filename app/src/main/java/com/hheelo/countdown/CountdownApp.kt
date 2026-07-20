package com.hheelo.countdown

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private const val FirstEventItemIndex = 3

@Composable
internal fun CountdownApp(
    selectedEventId: String?,
    selectedEventRequest: Int
) {
    val vm: CountdownViewModel = viewModel()
    val listState = rememberLazyListState()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val events = uiState.events
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }
    var notificationsGranted by remember {
        mutableStateOf(NotificationHelper.canPostNotifications(context))
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted || NotificationHelper.canPostNotifications(context)
        if (notificationsGranted) {
            vm.reschedulePersistedReminders()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refresh()
                val wasGranted = notificationsGranted
                val isGranted = NotificationHelper.canPostNotifications(context)
                notificationsGranted = isGranted
                if (isGranted && !wasGranted) {
                    vm.reschedulePersistedReminders()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(selectedEventId, selectedEventRequest, uiState.isLoading) {
        if (uiState.isLoading) return@LaunchedEffect
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
                .testTag("countdown-list")
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
            item { PreviewCards(uiState.snapshot.cards) }
            if (uiState.isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.loading_events),
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            } else {
                item {
                    CustomEditorHeader(
                        onAdd = { vm.add() },
                        enabled = !uiState.isSaving
                    )
                }
                itemsIndexed(events, key = { _, event -> event.id }) { index, event ->
                    CustomEventEditor(
                        event = event,
                        highlighted = event.id == selectedEventId,
                        canMoveUp = index > 0 && events[index - 1].isPinned == event.isPinned,
                        canMoveDown = index < events.lastIndex && events[index + 1].isPinned == event.isPinned,
                        onChange = { updated -> vm.update(index, updated) },
                        onDelete = { showDeleteDialog = index },
                        onMoveUp = { vm.moveUp(index) },
                        onMoveDown = { vm.moveDown(index) },
                        enabled = !uiState.isSaving,
                        notificationsGranted = notificationsGranted,
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onOpenNotificationSettings = {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                            )
                        }
                    )
                }
                if (events.isEmpty()) {
                    item { EmptyState() }
                }
                item {
                    SavePanel(
                        savedMessage = uiState.statusMessage,
                        tintHex = events.firstOrNull()?.colorHex ?: CountdownColorHex.Brand,
                        onSave = { vm.save() },
                        isSaving = uiState.isSaving,
                        messageIsError = uiState.statusMessageIsError,
                        enabled = !uiState.isLoading
                    )
                }
            }
            item { Footer() }
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(showDeleteDialog!!)
                    showDeleteDialog = null
                }) {
                    Text(stringResource(R.string.delete_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.delete_cancel_button))
                }
            }
        )
    }
}
