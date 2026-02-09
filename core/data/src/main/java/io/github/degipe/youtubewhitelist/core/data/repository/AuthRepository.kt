package io.github.degipe.youtubewhitelist.core.data.repository

import android.content.Context
import io.github.degipe.youtubewhitelist.core.data.model.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    suspend fun signIn(activityContext: Context)
    suspend fun signOut()
    suspend fun checkAuthState()
}
