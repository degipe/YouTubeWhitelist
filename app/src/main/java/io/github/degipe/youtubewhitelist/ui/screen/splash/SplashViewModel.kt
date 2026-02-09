package io.github.degipe.youtubewhitelist.ui.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.repository.ParentAccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data object FirstRun : SplashUiState
    data object ReturningUser : SplashUiState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val parentAccountRepository: ParentAccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkAppState()
    }

    private fun checkAppState() {
        viewModelScope.launch {
            val hasAccount = parentAccountRepository.hasAccount()
            _uiState.value = if (hasAccount) {
                SplashUiState.ReturningUser
            } else {
                SplashUiState.FirstRun
            }
        }
    }
}
