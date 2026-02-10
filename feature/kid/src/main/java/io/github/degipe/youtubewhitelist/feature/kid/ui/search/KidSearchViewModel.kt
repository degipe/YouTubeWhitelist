package io.github.degipe.youtubewhitelist.feature.kid.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class KidSearchUiState(
    val query: String = "",
    val results: List<WhitelistItem> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = KidSearchViewModel.Factory::class)
class KidSearchViewModel @AssistedInject constructor(
    private val whitelistRepository: WhitelistRepository,
    @Assisted private val profileId: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(profileId: String): KidSearchViewModel
    }

    private val queryFlow = MutableStateFlow("")

    val query: StateFlow<String> = queryFlow.asStateFlow()

    val uiState: StateFlow<KidSearchUiState> = queryFlow
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(KidSearchUiState(query = query))
            } else {
                whitelistRepository.searchItems(profileId, query)
                    .map { results ->
                        KidSearchUiState(
                            query = query,
                            results = results,
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
}
