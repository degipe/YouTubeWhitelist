package io.github.degipe.youtubewhitelist.feature.kid.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class KidSearchUiState(
    val query: String = "",
    val results: List<WhitelistItem> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = KidSearchViewModel.Factory::class)
class KidSearchViewModel @AssistedInject constructor(
    private val whitelistRepository: WhitelistRepository,
    private val youTubeApiRepository: YouTubeApiRepository,
    @Assisted private val profileId: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(profileId: String): KidSearchViewModel
    }

    private val queryFlow = MutableStateFlow("")
    private val _channelVideoResults = MutableStateFlow<List<WhitelistItem>>(emptyList())
    private var channelSearchJob: Job? = null

    val query: StateFlow<String> = queryFlow.asStateFlow()

    val uiState: StateFlow<KidSearchUiState> = queryFlow
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                _channelVideoResults.value = emptyList()
                channelSearchJob?.cancel()
                flowOf(KidSearchUiState(query = query))
            } else {
                searchChannels(query)
                combine(
                    whitelistRepository.searchItems(profileId, query),
                    _channelVideoResults
                ) { localResults, channelResults ->
                    val combined = (localResults + channelResults)
                        .distinctBy { it.youtubeId }
                    KidSearchUiState(
                        query = query,
                        results = combined,
                        isSearching = false
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = KidSearchUiState()
        )

    fun onQueryChanged(query: String) {
        queryFlow.value = query
    }

    fun onClearQuery() {
        queryFlow.value = ""
    }

    private fun searchChannels(query: String) {
        channelSearchJob?.cancel()
        channelSearchJob = viewModelScope.launch {
            _channelVideoResults.value = emptyList()
            val channelIds = whitelistRepository.getChannelYoutubeIds(profileId)
            if (channelIds.isEmpty()) return@launch

            val results = mutableListOf<WhitelistItem>()
            for (channelId in channelIds.take(3)) {
                when (val result = youTubeApiRepository.searchVideosInChannel(channelId, query)) {
                    is AppResult.Success -> {
                        results.addAll(result.data.map { video ->
                            WhitelistItem(
                                id = "search-${video.videoId}",
                                kidProfileId = profileId,
                                type = WhitelistItemType.VIDEO,
                                youtubeId = video.videoId,
                                title = video.title,
                                thumbnailUrl = video.thumbnailUrl,
                                channelTitle = video.channelTitle,
                                addedAt = 0L
                            )
                        })
                    }
                    is AppResult.Error -> { /* silently skip failed channel */ }
                }
            }
            _channelVideoResults.value = results
        }
    }
}
