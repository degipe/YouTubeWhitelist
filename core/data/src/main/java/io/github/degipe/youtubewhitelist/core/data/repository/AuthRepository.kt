package io.github.degipe.youtubewhitelist.core.data.repository

import android.content.Context
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.model.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    suspend fun signIn(activityContext: Context)
    suspend fun signOut()
    suspend fun checkAuthState()

    /**
     * Creates a local-only parent account (no Google identity), so onboarding can complete
     * on builds where Google OAuth is unavailable/unconfigured (e.g. F-Droid builds without
     * a GOOGLE_CLIENT_ID). Idempotent: if a parent account already exists, it is reused
     * instead of creating a duplicate.
     */
    suspend fun continueWithoutGoogle(): AppResult<Unit>
}
