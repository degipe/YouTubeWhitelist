package io.github.degipe.youtubewhitelist.feature.kid.ui.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.model.PlaylistVideo
import io.github.degipe.youtubewhitelist.core.data.repository.ChannelVideoCacheRepository
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChannelDetailUiState(
    val channelTitle: String = "",
    val videos: List<PlaylistVideo> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMorePages: Boolean = false
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = ChannelDetailViewModel.Factory::class)
class ChannelDetailViewModel @AssistedInject constructor(
    private val youTubeApiRepository: YouTubeApiRepository,
    private val channelVideoCacheRepository: ChannelVideoCacheRepository,
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var uploadsPlaylistId: String? = null
    private var nextPageToken: String? = null

    private data class ControlState(
        val isLoading: Boolean = true,
        val isLoadingMore: Boolean = false,
        val error: String? = null,
        val hasMorePages: Boolean = false
    )

    private val _controlState = MutableStateFlow(ControlState())

    private val videosFlow = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                channelVideoCacheRepository.getVideos(channelId)
            } else {
                channelVideoCacheRepository.searchVideos(channelId, query)
            }
        }

    val uiState: StateFlow<ChannelDetailUiState> = combine(
        videosFlow,
        _controlState
    ) { videos, ctrl ->
        ChannelDetailUiState(
            channelTitle = channelTitle,
            videos = videos,
            isLoading = ctrl.isLoading,
            isLoadingMore = ctrl.isLoadingMore,
            error = ctrl.error,
            hasMorePages = ctrl.hasMorePages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ChannelDetailUiState(channelTitle = channelTitle)
    )

    init {
        loadChannelVideos()
    }

    fun retry() {
        loadChannelVideos()
    }

    fun loadMore() {
        val token = nextPageToken
        if (token == null || _controlState.value.isLoadingMore) return

        _controlState.value = _controlState.value.copy(isLoadingMore = true)
        viewModelScope.launch {
            val playlistId = uploadsPlaylistId ?: return@launch
            when (val result = youTubeApiRepository.getPlaylistItemsPage(playlistId, token)) {
                is AppResult.Success -> {
                    val page = result.data
                    channelVideoCacheRepository.cacheVideos(channelId, page.videos)
                    nextPageToken = page.nextPageToken
                    _controlState.value = _controlState.value.copy(
                        isLoadingMore = false,
                        hasMorePages = page.nextPageToken != null
                    )
                }
                is AppResult.Error -> {
                    _controlState.value = _controlState.value.copy(
                        isLoadingMore = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onClearSearch() {
        _searchQuery.value = ""
    }

    private fun loadChannelVideos() {
        _controlState.value = ControlState(isLoading = true)
        nextPageToken = null
        viewModelScope.launch {
            channelVideoCacheRepository.clearCache(channelId)

            when (val channelResult = youTubeApiRepository.getChannelById(channelId)) {
                is AppResult.Success -> {
                    val playlistId = channelResult.data.uploadsPlaylistId
                    if (playlistId == null) {
                        _controlState.value = ControlState(
                            isLoading = false,
                            error = "Channel uploads playlist not found"
                        )
                        return@launch
                    }
                    uploadsPlaylistId = playlistId

                    when (val videosResult = youTubeApiRepository.getPlaylistItemsPage(playlistId, null)) {
                        is AppResult.Success -> {
                            val page = videosResult.data
                            channelVideoCacheRepository.cacheVideos(channelId, page.videos)
                            nextPageToken = page.nextPageToken
                            _controlState.value = ControlState(
                                isLoading = false,
                                hasMorePages = page.nextPageToken != null
                            )
                        }
                        is AppResult.Error -> {
                            _controlState.value = ControlState(
                                isLoading = false,
                                error = videosResult.message
                            )
                        }
                    }
                }
                is AppResult.Error -> {
                    _controlState.value = ControlState(
                        isLoading = false,
                        error = channelResult.message
                    )
                }
            }
        }
    }
}
