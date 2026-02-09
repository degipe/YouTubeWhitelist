package io.github.degipe.youtubewhitelist.ui.screen.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SignInUiState {
    data object Idle : SignInUiState
    data object Loading : SignInUiState
    data object Success : SignInUiState
    data class Error(val message: String) : SignInUiState
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun signIn(activityContext: Context) {
        viewModelScope.launch {
            _uiState.value = SignInUiState.Loading
            try {
                authRepository.signIn(activityContext)
                _uiState.value = SignInUiState.Success
            } catch (e: Exception) {
                _uiState.value = SignInUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
