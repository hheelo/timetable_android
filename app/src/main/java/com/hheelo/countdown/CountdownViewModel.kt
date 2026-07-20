package com.hheelo.countdown

import android.app.Application
import com.hheelo.countdown.logging.AppLog
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class CountdownViewModel(application: Application) : AndroidViewModel(application) {
    private companion object {
        const val TAG = "CountdownViewModel"
    }

    private val ctx = application.applicationContext
    private val store = CountdownStore(application)

    private val _uiState = MutableStateFlow(
        CountdownUiState(
            events = emptyList(),
            snapshot = CountdownSnapshot(LocalDate.now(), CountdownCalculator.makeDefaultCards(application)),
            isLoading = true,
            isSaving = false,
            statusMessage = null,
            statusMessageIsError = false
        )
    )
    val uiState: StateFlow<CountdownUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { store.loadCustomEvents() }
            }.onSuccess { loaded ->
                _uiState.value = _uiState.value.copy(
                    events = loaded,
                    snapshot = CountdownCalculator.makePreviewSnapshot(ctx, loaded),
                    isLoading = false
                )
            }.onFailure {
                AppLog.e(TAG, "加载事件失败，使用空列表", it)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        statusMessage = getApplication<Application>().getString(R.string.load_failure_message),
                        statusMessageIsError = true
                    )
                }
            }
        }
    }

    fun add() {
        if (!canEdit()) return
        val events = _uiState.value.events
        val offset = 7L * (events.size + 1)
        updateEvents(events + CountdownEvent.empty(getApplication(), offset))
    }

    fun update(index: Int, updated: CountdownEvent) {
        if (!canEdit()) return
        val events = _uiState.value.events
        if (index !in events.indices) return
        val previous = events[index]
        val replaced = events.replaced(index, updated)
        updateEvents(if (previous.isPinned != updated.isPinned) replaced.groupedByPin() else replaced)
    }

    fun delete(index: Int) {
        if (!canEdit()) return
        val events = _uiState.value.events
        if (index !in events.indices) return
        updateEvents(events.removedAt(index))
    }

    fun moveUp(index: Int) {
        if (!canEdit()) return
        val events = _uiState.value.events
        if (index <= 0 || index >= events.size) return
        if (events[index].isPinned != events[index - 1].isPinned) return
        updateEvents(events.moved(index, index - 1))
    }

    fun moveDown(index: Int) {
        if (!canEdit()) return
        val events = _uiState.value.events
        if (index < 0 || index >= events.lastIndex) return
        if (events[index].isPinned != events[index + 1].isPinned) return
        updateEvents(events.moved(index, index + 1))
    }

    fun save() {
        val state = _uiState.value
        if (state.isLoading || state.isSaving) return
        val current = state.events
        _uiState.update {
            it.copy(isSaving = true, statusMessage = null, statusMessageIsError = false)
        }
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    store.saveCustomEvents(current)
                    ReminderScheduler.sync(ctx, current)
                    CountdownWidget().updateAll(getApplication())
                }
            }.onSuccess {
                AppLog.i(TAG, "保存并刷新小组件完成，共 ${current.size} 个事件")
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        statusMessage = getApplication<Application>().getString(R.string.save_success_message),
                        statusMessageIsError = false
                    )
                }
            }.onFailure {
                AppLog.e(TAG, "保存或刷新小组件失败", it)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        statusMessage = getApplication<Application>().getString(R.string.save_failure_message),
                        statusMessageIsError = true
                    )
                }
            }
        }
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(snapshot = CountdownCalculator.makePreviewSnapshot(ctx, it.events))
        }
    }

    fun reschedulePersistedReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                ReminderScheduler.sync(ctx, store.loadCustomEvents())
            }.onFailure {
                AppLog.w(TAG, "重新同步提醒失败", it)
            }
        }
    }

    private fun updateEvents(updated: List<CountdownEvent>) {
        _uiState.update {
            it.copy(
                events = updated,
                snapshot = CountdownCalculator.makePreviewSnapshot(ctx, updated),
                statusMessage = null,
                statusMessageIsError = false
            )
        }
    }

    private fun canEdit(): Boolean {
        return !_uiState.value.isLoading && !_uiState.value.isSaving
    }

    private fun List<CountdownEvent>.groupedByPin(): List<CountdownEvent> {
        return filter { it.isPinned } + filterNot { it.isPinned }
    }
}
