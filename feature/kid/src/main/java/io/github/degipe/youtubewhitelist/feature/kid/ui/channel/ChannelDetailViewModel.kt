package io.github.degipe.youtubewhitelist.feature.kid.ui.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ChannelDetailUiState(
    val channelTitle: String = "",
    val videos: List<WhitelistItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel(assistedFactory = ChannelDetailViewModel.Factory::class)
class ChannelDetailViewModel @AssistedInject constructor(
    whitelistRepository: WhitelistRepository,
    @Assisted("profileId") private val profileId: String,
    @Assisted("channelTitle") private val channelTitle: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("profileId") profileId: String,
            @Assisted("channelTitle") channelTitle: String
        ): ChannelDetailViewModel
    }

    val uiState: StateFlow<ChannelDetailUiState> = whitelistRepository
        .getVideosByChannelTitle(profileId, channelTitle)
        .map { videos ->
            ChannelDetailUiState(
                channelTitle = channelTitle,
                videos = videos,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ChannelDetailUiState(channelTitle = channelTitle)
        )
}
