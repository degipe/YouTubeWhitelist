package io.github.degipe.youtubewhitelist.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.model.KidProfile
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.ParentAccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileSelectorUiState(
    val profiles: List<KidProfile> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileSelectorViewModel @Inject constructor(
    private val parentAccountRepository: ParentAccountRepository,
    private val kidProfileRepository: KidProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSelectorUiState())
    val uiState: StateFlow<ProfileSelectorUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun loadProfiles() {
        viewModelScope.launch {
            parentAccountRepository.getAccount()
                .flatMapLatest { account ->
                    if (account == null) flowOf(emptyList())
                    else kidProfileRepository.getProfilesByParent(account.id)
                }
                .collect { profiles ->
                    _uiState.value = ProfileSelectorUiState(
                        profiles = profiles,
                        isLoading = false
                    )
                }
        }
    }
}
