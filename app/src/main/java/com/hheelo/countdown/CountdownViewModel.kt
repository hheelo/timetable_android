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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class CountdownViewModel(application: Application) : AndroidViewModel(application) {
    private companion object {
        const val TAG = "CountdownViewModel"
    }

    private val ctx = application.applicationContext
    private val store = CountdownStore(application)

    private val _events = MutableStateFlow<List<CountdownEvent>>(emptyList())
    val events: StateFlow<List<CountdownEvent>> = _events.asStateFlow()

    private val _snapshot = MutableStateFlow(CountdownSnapshot(LocalDate.now(), CountdownCalculator.makeDefaultCards(application)))
    val snapshot: StateFlow<CountdownSnapshot> = _snapshot.asStateFlow()

    private val _savedMessage = MutableStateFlow<String?>(null)
    val savedMessage: StateFlow<String?> = _savedMessage.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { store.loadCustomEvents() }
            }.onSuccess { loaded ->
                _events.value = loaded
                _snapshot.value = CountdownCalculator.makePreviewSnapshot(ctx, loaded)
            }.onFailure {
                AppLog.e(TAG, "加载事件失败，使用空列表", it)
            }
        }
    }

    fun add() {
        val offset = 7L * (_events.value.size + 1)
        updateEvents(_events.value + CountdownEvent.empty(getApplication(), offset))
    }

    fun update(index: Int, updated: CountdownEvent) {
        if (index !in _events.value.indices) return
        updateEvents(_events.value.replaced(index, updated))
    }

    fun delete(index: Int) {
        if (index !in _events.value.indices) return
        updateEvents(_events.value.removedAt(index))
    }

    fun moveUp(index: Int) {
        if (index <= 0 || index >= _events.value.size) return
        updateEvents(_events.value.moved(index, index - 1))
    }

    fun moveDown(index: Int) {
        if (index < 0 || index >= _events.value.lastIndex) return
        updateEvents(_events.value.moved(index, index + 1))
    }

    fun save() {
        val current = _events.value
        _savedMessage.value = getApplication<Application>().getString(R.string.save_success_message)
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
                _savedMessage.value = getApplication<Application>().getString(R.string.save_failure_message)
            }
        }
    }

    fun refresh() {
        _snapshot.value = CountdownCalculator.makePreviewSnapshot(ctx, _events.value)
    }

    private fun updateEvents(updated: List<CountdownEvent>) {
        _events.value = updated
        _savedMessage.value = null
        _snapshot.value = CountdownCalculator.makePreviewSnapshot(ctx, updated)
    }
}
