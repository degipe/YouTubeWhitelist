package io.github.degipe.youtubewhitelist.feature.sleep.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TimerState {
    SELECTING,
    RUNNING,
    EXPIRED
}

data class SleepModeUiState(
    val timerState: TimerState = TimerState.SELECTING,
    val selectedDurationMinutes: Int = 30,
    val remainingSeconds: Long = 0,
    val fadeVolume: Float = 1f,
    val currentVideoIndex: Int = 0,
    val videos: List<WhitelistItem> = emptyList(),
    val isLoading: Boolean = true
) {
    val currentVideo: WhitelistItem?
        get() = videos.getOrNull(currentVideoIndex)
}

@HiltViewModel(assistedFactory = SleepModeViewModel.Factory::class)
class SleepModeViewModel @AssistedInject constructor(
    private val whitelistRepository: WhitelistRepository,
    @Assisted private val profileId: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(profileId: String): SleepModeViewModel
    }

    companion object {
        const val FADE_DURATION_SECONDS = 120L // 2 minutes
    }

    private val _uiState = MutableStateFlow(SleepModeUiState())
    val uiState: StateFlow<SleepModeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadVideos()
    }

    private fun loadVideos() {
        viewModelScope.launch {
            val videos = whitelistRepository.getVideosByProfile(profileId).first()
            _uiState.update { it.copy(videos = videos, isLoading = false) }
        }
    }

    fun selectDuration(minutes: Int) {
        _uiState.update { it.copy(selectedDurationMinutes = minutes) }
    }

    fun startTimer() {
        val durationSeconds = _uiState.value.selectedDurationMinutes * 60L
        _uiState.update {
            it.copy(
                timerState = TimerState.RUNNING,
                remainingSeconds = durationSeconds,
                fadeVolume = 1f,
                currentVideoIndex = 0
            )
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000)
                _uiState.update { state ->
                    val newRemaining = (state.remainingSeconds - 1).coerceAtLeast(0)
                    val newVolume = if (newRemaining <= FADE_DURATION_SECONDS) {
                        newRemaining.toFloat() / FADE_DURATION_SECONDS
                    } else {
                        1f
                    }
                    state.copy(
                        remainingSeconds = newRemaining,
                        fadeVolume = newVolume
                    )
                }
            }
            _uiState.update { it.copy(timerState = TimerState.EXPIRED) }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update {
            it.copy(
                timerState = TimerState.SELECTING,
                remainingSeconds = 0,
                fadeVolume = 1f
            )
        }
    }

    fun onVideoEnded() {
        if (_uiState.value.timerState == TimerState.EXPIRED) return
        val videos = _uiState.value.videos
        if (videos.isEmpty()) return

        _uiState.update {
            it.copy(currentVideoIndex = (it.currentVideoIndex + 1) % videos.size)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
