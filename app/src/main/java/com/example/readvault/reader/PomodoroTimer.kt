package com.example.readvault.reader

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PomodoroTimer(private val scope: CoroutineScope) {
    private var timerJob: Job? = null

    private val _totalSeconds = MutableStateFlow(25 * 60)
    val totalSeconds = _totalSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25 * 60)
    val remainingSeconds = _remainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    fun startTimer(minutes: Int, onComplete: () -> Unit = {}) {
        stopTimer()
        _totalSeconds.value = minutes * 60
        _remainingSeconds.value = minutes * 60
        _isRunning.value = true

        timerJob = scope.launch {
            while (_remainingSeconds.value > 0 && _isRunning.value) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            if (_remainingSeconds.value <= 0) {
                _isRunning.value = false
                onComplete()
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun resumeTimer(onComplete: () -> Unit = {}) {
        if (_remainingSeconds.value > 0) {
            _isRunning.value = true
            timerJob = scope.launch {
                while (_remainingSeconds.value > 0 && _isRunning.value) {
                    delay(1000)
                    _remainingSeconds.value -= 1
                }
                if (_remainingSeconds.value <= 0) {
                    _isRunning.value = false
                    onComplete()
                }
            }
        }
    }

    fun stopTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        timerJob = null
        _remainingSeconds.value = _totalSeconds.value
    }

    val formattedTime: String
        get() {
            val sec = _remainingSeconds.value
            val m = sec / 60
            val s = sec % 60
            return "%02d:%02d".format(m, s)
        }
}
