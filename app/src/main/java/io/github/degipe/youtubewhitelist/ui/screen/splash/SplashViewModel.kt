package io.github.degipe.youtubewhitelist.ui.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.ParentAccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data object FirstRun : SplashUiState
    data class ReturningUser(val profileId: String) : SplashUiState
    data object MultipleProfiles : SplashUiState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val parentAccountRepository: ParentAccountRepository,
    private val kidProfileRepository: KidProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkAppState()
    }

    private fun checkAppState() {
        viewModelScope.launch {
            val hasAccount = parentAccountRepository.hasAccount()
            if (!hasAccount) {
                _uiState.value = SplashUiState.FirstRun
                return@launch
            }

            val account = parentAccountRepository.getAccount().first()
            if (account == null) {
                _uiState.value = SplashUiState.FirstRun
                return@launch
            }

            val profiles = kidProfileRepository.getProfilesByParent(account.id).first()
            if (profiles.isEmpty()) {
                _uiState.value = SplashUiState.FirstRun
                return@launch
            }

            if (profiles.size > 1) {
                _uiState.value = SplashUiState.MultipleProfiles
            } else {
                _uiState.value = SplashUiState.ReturningUser(profileId = profiles.first().id)
            }
        }
    }
}
