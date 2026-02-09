package io.github.degipe.youtubewhitelist.core.auth.google

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

/**
 * Mock implementation of GoogleSignInManager.
 * Returns a placeholder success result until Google Cloud Console is configured
 * with OAuth client ID and SHA-1 fingerprint.
 *
 * TODO: Replace with real Credential Manager / Google Sign-In API integration
 */
class GoogleSignInManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GoogleSignInManager {

    private var signedIn = false

    override suspend fun signIn(activityContext: Context): GoogleSignInResult {
        // Mock: simulate successful Google Sign-In
        signedIn = true
        return GoogleSignInResult.Success(
            googleAccountId = "mock_${UUID.randomUUID().toString().take(8)}",
            email = "parent@example.com",
            displayName = "Test Parent",
            accessToken = "mock_access_token_${System.currentTimeMillis()}",
            refreshToken = "mock_refresh_token_${System.currentTimeMillis()}"
        )
    }

    override suspend fun signOut() {
        signedIn = false
    }

    override fun isSignedIn(): Boolean = signedIn
}
