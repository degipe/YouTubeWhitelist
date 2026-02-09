package io.github.degipe.youtubewhitelist.feature.parent.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.model.KidProfile
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.ParentAccountRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentDashboardUiState(
    val profiles: List<KidProfile> = emptyList(),
    val selectedProfileId: String? = null,
    val parentAccountId: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val parentAccountRepository: ParentAccountRepository,
    private val kidProfileRepository: KidProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState: StateFlow<ParentDashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            parentAccountRepository.getAccount()
                .flatMapLatest { account ->
                    if (account == null) {
                        flowOf(Pair<String?, List<KidProfile>>(null, emptyList()))
                    } else {
                        kidProfileRepository.getProfilesByParent(account.id)
                            .map { profiles ->
                                Pair<String?, List<KidProfile>>(account.id, profiles)
                            }
                    }
                }
                .collect { (accountId, profiles) ->
                    _uiState.update { state ->
                        val selectedId = when {
                            state.selectedProfileId != null &&
                                profiles.any { it.id == state.selectedProfileId } ->
                                state.selectedProfileId
                            profiles.isNotEmpty() -> profiles.first().id
                            else -> null
                        }
                        state.copy(
                            profiles = profiles,
                            selectedProfileId = selectedId,
                            parentAccountId = accountId,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun selectProfile(profileId: String) {
        _uiState.update { it.copy(selectedProfileId = profileId) }
    }
}
