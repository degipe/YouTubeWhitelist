package io.github.degipe.youtubewhitelist.feature.kid.ui.playlist

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

data class PlaylistDetailUiState(
    val videos: List<PlaylistVideo> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel(assistedFactory = PlaylistDetailViewModel.Factory::class)
class PlaylistDetailViewModel @AssistedInject constructor(
    private val youTubeApiRepository: YouTubeApiRepository,
    @Assisted("profileId") private val profileId: String,
    @Assisted("playlistId") private val playlistId: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("profileId") profileId: String,
            @Assisted("playlistId") playlistId: String
        ): PlaylistDetailViewModel
    }

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    init {
        loadPlaylistItems()
    }

    fun retry() {
        loadPlaylistItems()
    }

    private fun loadPlaylistItems() {
        _uiState.value = PlaylistDetailUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = youTubeApiRepository.getPlaylistItems(playlistId)) {
                is AppResult.Success -> {
                    _uiState.value = PlaylistDetailUiState(
                        videos = result.data.sortedBy { it.position },
                        isLoading = false
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = PlaylistDetailUiState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}
