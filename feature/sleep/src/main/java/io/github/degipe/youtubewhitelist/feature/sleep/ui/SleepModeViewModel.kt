package io.github.degipe.youtubewhitelist.feature.sleep.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerManager
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SleepModeUiState(
    val selectedDurationMinutes: Int = 30,
    val timerStatus: SleepTimerStatus = SleepTimerStatus.IDLE,
    val remainingSeconds: Long = 0,
    val formattedRemaining: String = "",
    val isTimerForThisProfile: Boolean = false
)

@HiltViewModel(assistedFactory = SleepModeViewModel.Factory::class)
class SleepModeViewModel @AssistedInject constructor(
    private val sleepTimerManager: SleepTimerManager,
    @Assisted private val profileId: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(profileId: String): SleepModeViewModel
    }

    private val _selectedMinutes = MutableStateFlow(30)

    val uiState: StateFlow<SleepModeUiState> = combine(
        _selectedMinutes,
        sleepTimerManager.state
    ) { selectedMinutes, timerState ->
        SleepModeUiState(
            selectedDurationMinutes = selectedMinutes,
            timerStatus = timerState.status,
            remainingSeconds = timerState.remainingSeconds,
            formattedRemaining = timerState.formattedRemaining,
            isTimerForThisProfile = timerState.profileId == profileId
                    && timerState.status != SleepTimerStatus.IDLE
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SleepModeUiState()
    )

    fun selectDuration(minutes: Int) {
        _selectedMinutes.update { minutes.coerceIn(5, 600) }
    }

    fun startTimer() {
        sleepTimerManager.startTimer(profileId, _selectedMinutes.value)
    }

    fun stopTimer() {
        sleepTimerManager.stopTimer()
    }
}
