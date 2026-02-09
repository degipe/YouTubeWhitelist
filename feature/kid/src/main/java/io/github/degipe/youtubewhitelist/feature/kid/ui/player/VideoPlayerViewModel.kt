package io.github.degipe.youtubewhitelist.feature.kid.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.repository.WatchHistoryRepository
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import io.github.degipe.youtubewhitelist.core.data.timelimit.TimeLimitChecker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VideoPlayerUiState(
    val videoId: String = "",
    val videoTitle: String = "",
    val youtubeId: String = "",
    val siblingVideos: List<WhitelistItem> = emptyList(),
    val currentIndex: Int = -1,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val remainingTimeFormatted: String? = null,
    val isTimeLimitReached: Boolean = false
)

@HiltViewModel(assistedFactory = VideoPlayerViewModel.Factory::class)
class VideoPlayerViewModel @AssistedInject constructor(
    private val whitelistRepository: WhitelistRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val timeLimitChecker: TimeLimitChecker,
    @Assisted("profileId") private val profileId: String,
    @Assisted("videoId") private val videoId: String,
    @Assisted("channelTitle") private val channelTitle: String?
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("profileId") profileId: String,
            @Assisted("videoId") videoId: String,
            @Assisted("channelTitle") channelTitle: String?
        ): VideoPlayerViewModel
    }

    private val _uiState = MutableStateFlow(VideoPlayerUiState(videoId = videoId))
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()

    private var siblingsJob: Job? = null

    init {
        loadVideo()
        loadSiblings()
        observeTimeLimit()
    }

    private fun loadVideo() {
        viewModelScope.launch {
            whitelistRepository.getItemById(videoId)
                .collect { item ->
                    if (item != null) {
                        _uiState.value = _uiState.value.copy(
                            videoTitle = item.title,
                            youtubeId = item.youtubeId,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun loadSiblings() {
        if (channelTitle == null) return

        siblingsJob?.cancel()
        siblingsJob = viewModelScope.launch {
            whitelistRepository.getVideosByChannelTitle(profileId, channelTitle)
                .collect { siblings ->
                    val currentIdx = siblings.indexOfFirst { it.id == _uiState.value.videoId }
                    _uiState.value = _uiState.value.copy(
                        siblingVideos = siblings,
                        currentIndex = currentIdx,
                        hasNext = currentIdx >= 0 && currentIdx < siblings.size - 1,
                        hasPrevious = currentIdx > 0
                    )
                }
        }
    }

    private fun observeTimeLimit() {
        viewModelScope.launch {
            timeLimitChecker.getTimeLimitStatus(profileId).collect { status ->
                _uiState.value = _uiState.value.copy(
                    remainingTimeFormatted = status.remainingSeconds?.let { formatRemaining(it) },
                    isTimeLimitReached = status.isLimitReached
                )
            }
        }
    }

    fun onVideoEnded(watchedSeconds: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            watchHistoryRepository.recordWatch(
                profileId = profileId,
                videoId = state.youtubeId,
                videoTitle = state.videoTitle,
                watchedSeconds = watchedSeconds
            )

            if (state.hasNext) {
                navigateToIndex(state.currentIndex + 1)
            }
        }
    }

    fun playNext() {
        val state = _uiState.value
        if (state.hasNext) {
            navigateToIndex(state.currentIndex + 1)
        }
    }

    fun playPrevious() {
        val state = _uiState.value
        if (state.hasPrevious) {
            navigateToIndex(state.currentIndex - 1)
        }
    }

    fun playVideoAt(index: Int) {
        navigateToIndex(index)
    }

    private fun navigateToIndex(index: Int) {
        val siblings = _uiState.value.siblingVideos
        if (index < 0 || index >= siblings.size) return

        val nextItem = siblings[index]
        _uiState.value = _uiState.value.copy(
            videoId = nextItem.id,
            videoTitle = nextItem.title,
            youtubeId = nextItem.youtubeId,
            currentIndex = index,
            hasNext = index < siblings.size - 1,
            hasPrevious = index > 0
        )
    }

    private fun formatRemaining(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}
