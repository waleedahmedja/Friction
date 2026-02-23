package com.waleedahmedja.friction.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.waleedahmedja.friction.messages.CompletionMessages
import com.waleedahmedja.friction.messages.ReflectiveMessages
import com.waleedahmedja.friction.data.DataStoreManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class LockState(
    val isActive       : Boolean = false,
    val unlockAt       : Long    = 0L,
    val totalDurationMs: Long    = 0L,
    val remainingMs    : Long    = 0L
) {
    val progressFraction: Float
        get() = if (totalDurationMs <= 0L) 0f
        else (1f - remainingMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)

    val sessionEndTime: String
        get() {
            if (unlockAt == 0L) return ""
            return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(unlockAt))
        }
}

data class TapState(
    val required : Int     = 75,
    val current  : Int     = 0,
    val done     : Boolean = false
)

data class CompletionState(
    val visible       : Boolean = false,
    val message       : String  = "",
    val durationLabel : String  = ""
)

class FrictionViewModel(app: Application) : AndroidViewModel(app) {

    private val ds = DataStoreManager(app.applicationContext)

    private val _hours   = MutableStateFlow(0)
    val hours: StateFlow<Int> = _hours.asStateFlow()

    private val _minutes = MutableStateFlow(25)
    val minutes: StateFlow<Int> = _minutes.asStateFlow()

    val totalMinutes: StateFlow<Int> = combine(_hours, _minutes) { h, m -> h * 60 + m }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 25)

    private val _lock = MutableStateFlow(LockState())
    val lock: StateFlow<LockState> = _lock.asStateFlow()

    private var timerJob: Job? = null

    private val _isStandby = MutableStateFlow(false)
    val isStandby: StateFlow<Boolean> = _isStandby.asStateFlow()

    private val _tap = MutableStateFlow(TapState())
    val tap: StateFlow<TapState> = _tap.asStateFlow()

    private val _reflectionMessage      = MutableStateFlow("")
    val reflectionMessage: StateFlow<String> = _reflectionMessage.asStateFlow()

    private val _showReflectionContinue = MutableStateFlow(false)
    val showReflectionContinue: StateFlow<Boolean> = _showReflectionContinue.asStateFlow()

    private val _completion = MutableStateFlow(CompletionState())
    val completion: StateFlow<CompletionState> = _completion.asStateFlow()

    init {
        viewModelScope.launch {
            val active     = ds.lockActive.first()
            val unlockAt   = ds.unlockTime.first()
            val totalDurMs = ds.totalDuration.first()
            if (active && unlockAt > 0L) {
                val rem = unlockAt - System.currentTimeMillis()
                if (rem > 0L) {
                    _lock.value = LockState(true, unlockAt, totalDurMs, rem)
                    runTimer(unlockAt, totalDurMs)
                } else {
                    ds.clearLock()
                }
            }
        }
    }

    fun setHours(h: Int)   { _hours.value   = h.coerceIn(0, 24) }
    fun setMinutes(m: Int) { _minutes.value = m.coerceIn(0, 59) }

    fun startLock() {
        val durationMs = (_hours.value * 3600L + _minutes.value * 60L) * 1_000L
        if (durationMs <= 0L) return
        val unlockAt = System.currentTimeMillis() + durationMs
        viewModelScope.launch {
            ds.startLock(unlockAt, durationMs)
            _lock.value = LockState(true, unlockAt, durationMs, durationMs)
            runTimer(unlockAt, durationMs)
        }
    }

    fun runTimer(unlockAt: Long, totalDurMs: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val rem = unlockAt - System.currentTimeMillis()
                if (rem <= 0L) {
                    _lock.value = _lock.value.copy(remainingMs = 0L, isActive = false)
                    ds.clearLock()
                    break
                }
                _lock.value = _lock.value.copy(remainingMs = rem)
                delay(1_000L)
            }
        }
    }

    fun pauseTimer() { timerJob?.cancel(); timerJob = null }

    fun resumeTimer() {
        val s = _lock.value
        if (s.isActive && s.unlockAt > 0L) runTimer(s.unlockAt, s.totalDurationMs)
    }

    fun enterStandby() { _isStandby.value = true }
    fun exitStandby()  { _isStandby.value = false }

    // Tap formula: max(75, totalMinutes * 12), capped at 300
    // 1m→75, 5m→75, 10m→120, 25m→300
    fun initTap() {
        val mins     = totalMinutes.value
        val required = (mins * 12).coerceIn(75, 300)
        _tap.value   = TapState(required = required, current = 0, done = false)
    }

    fun recordEarlyExit() {
        viewModelScope.launch {
            pauseTimer()
            ds.clearLock()
            _lock.value      = LockState()
            _isStandby.value = false
        }
    }

    fun registerTap() {
        val s    = _tap.value
        if (s.done) return
        val next = (s.current + 1).coerceAtMost(s.required)
        _tap.value = s.copy(current = next, done = next >= s.required)
    }

    fun loadReflectionMessage() {
        viewModelScope.launch {
            val idx = ds.msgIndex.first()
            _reflectionMessage.value      = ReflectiveMessages.getForIndex(idx)
            _showReflectionContinue.value = false
            delay(1_500L)
            _showReflectionContinue.value = true
            ds.saveMsgIndex(ReflectiveMessages.nextIndex(idx))
        }
    }

    fun clearReflectionContinue() { _showReflectionContinue.value = false }

    fun showCompletion(totalMins: Int) {
        viewModelScope.launch {
            val label = CompletionMessages.durationLabel(totalMins)
            val idx   = ds.msgIndex.first()
            val msg   = CompletionMessages.getForIndex(idx, label)
            ds.saveMsgIndex(CompletionMessages.nextIndex(idx))
            _completion.value = CompletionState(true, msg, label)
        }
    }

    fun clearCompletion() { _completion.value = CompletionState() }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}