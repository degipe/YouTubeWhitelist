package io.github.degipe.youtubewhitelist.core.auth.google

import android.content.Context

interface GoogleSignInManager {
    suspend fun signIn(activityContext: Context): GoogleSignInResult
    suspend fun signOut()
    fun isSignedIn(): Boolean
}
