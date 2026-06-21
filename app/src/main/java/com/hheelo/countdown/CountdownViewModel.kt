package com.hheelo.countdown

import android.app.Application
import com.hheelo.countdown.logging.AppLog
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
    private companion object {
        const val TAG = "CountdownViewModel"
    }

    private val ctx = application.applicationContext
    private val store = CountdownStore(application)

    var events by mutableStateOf<List<CountdownEvent>>(emptyList())
        private set

    var snapshot by mutableStateOf(CountdownSnapshot(LocalDate.now(), CountdownCalculator.makeDefaultCards(application)))
        private set

    var savedMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { store.loadCustomEvents() }
            }.onSuccess { loaded ->
                events = loaded
                snapshot = CountdownCalculator.makePreviewSnapshot(ctx, loaded)
            }.onFailure {
                AppLog.e(TAG, "加载事件失败，使用空列表", it)
            }
        }
    }

    fun add() {
        val offset = 7L * (events.size + 1)
        updateEvents(events + CountdownEvent.empty(getApplication(), offset))
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
        savedMessage = getApplication<Application>().getString(R.string.save_success_message)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    store.saveCustomEvents(current)
                    CountdownWidget().updateAll(getApplication())
                }
            }.onSuccess {
                AppLog.i(TAG, "保存并刷新小组件完成，共 ${current.size} 个事件")
            }.onFailure {
                AppLog.e(TAG, "保存或刷新小组件失败", it)
                savedMessage = getApplication<Application>().getString(R.string.save_failure_message)
            }
        }
    }

    fun refresh() {
        snapshot = CountdownCalculator.makePreviewSnapshot(ctx, events)
    }

    private fun updateEvents(updated: List<CountdownEvent>) {
        events = updated
        savedMessage = null
        snapshot = CountdownCalculator.makePreviewSnapshot(ctx, updated)
    }
}
