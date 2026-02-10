package io.github.degipe.youtubewhitelist.feature.kid.ui.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.model.PlaylistVideo
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChannelDetailUiState(
    val channelTitle: String = "",
    val videos: List<PlaylistVideo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel(assistedFactory = ChannelDetailViewModel.Factory::class)
class ChannelDetailViewModel @AssistedInject constructor(
    private val youTubeApiRepository: YouTubeApiRepository,
    @Assisted("channelId") private val channelId: String,
    @Assisted("channelTitle") private val channelTitle: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("channelId") channelId: String,
            @Assisted("channelTitle") channelTitle: String
        ): ChannelDetailViewModel
    }

    private val _uiState = MutableStateFlow(ChannelDetailUiState(channelTitle = channelTitle))
    val uiState: StateFlow<ChannelDetailUiState> = _uiState.asStateFlow()

    init {
        loadChannelVideos()
    }

    fun retry() {
        loadChannelVideos()
    }

    private fun loadChannelVideos() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            // Step 1: Get channel info to find the uploads playlist ID
            when (val channelResult = youTubeApiRepository.getChannelById(channelId)) {
                is AppResult.Success -> {
                    val uploadsPlaylistId = channelResult.data.uploadsPlaylistId
                    if (uploadsPlaylistId == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Channel uploads playlist not found"
                        )
                        return@launch
                    }

                    // Step 2: Get videos from the uploads playlist
                    when (val videosResult = youTubeApiRepository.getPlaylistItems(uploadsPlaylistId)) {
                        is AppResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                videos = videosResult.data.sortedBy { it.position }
                            )
                        }
                        is AppResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = videosResult.message
                            )
                        }
                    }
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = channelResult.message
                    )
                }
            }
        }
    }
}
