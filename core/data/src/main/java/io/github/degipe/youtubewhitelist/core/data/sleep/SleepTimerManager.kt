package io.github.degipe.youtubewhitelist.core.data.sleep

import kotlinx.coroutines.flow.StateFlow

enum class SleepTimerStatus {
    IDLE,
    RUNNING,
    EXPIRED
}

data class SleepTimerState(
    val status: SleepTimerStatus = SleepTimerStatus.IDLE,
    val profileId: String? = null,
    val totalDurationMinutes: Int = 0,
    val remainingSeconds: Long = 0
) {
    val formattedRemaining: String
        get() {
            val h = remainingSeconds / 3600
            val m = (remainingSeconds % 3600) / 60
            return if (h > 0) "${h}h ${m}m" else "${m}m"
        }
}

interface SleepTimerManager {
    val state: StateFlow<SleepTimerState>
    fun startTimer(profileId: String, durationMinutes: Int)
    fun stopTimer()
}
