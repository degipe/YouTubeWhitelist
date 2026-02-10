package io.github.degipe.youtubewhitelist.core.data.sleep

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SleepTimerManagerImpl(
    private val scope: CoroutineScope
) : SleepTimerManager {

    private val _state = MutableStateFlow(SleepTimerState())
    override val state: StateFlow<SleepTimerState> = _state.asStateFlow()

    private var timerJob: Job? = null

    override fun startTimer(profileId: String, durationMinutes: Int) {
        timerJob?.cancel()
        val durationSeconds = durationMinutes * 60L
        _state.update {
            SleepTimerState(
                status = SleepTimerStatus.RUNNING,
                profileId = profileId,
                totalDurationMinutes = durationMinutes,
                remainingSeconds = durationSeconds
            )
        }
        timerJob = scope.launch {
            while (_state.value.remainingSeconds > 0) {
                delay(1000)
                _state.update { it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0)) }
            }
            _state.update { it.copy(status = SleepTimerStatus.EXPIRED) }
        }
    }

    override fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _state.update { SleepTimerState() }
    }
}
