package io.github.degipe.youtubewhitelist.core.data.model

sealed interface AuthState {
    data object Loading : AuthState
    data class Authenticated(val account: ParentAccount) : AuthState
    data object Unauthenticated : AuthState
}
