package io.github.degipe.youtubewhitelist.core.auth.google

sealed class GoogleSignInResult {
    data class Success(
        val googleAccountId: String,
        val email: String,
        val displayName: String?,
        val accessToken: String,
        val refreshToken: String?
    ) : GoogleSignInResult()

    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : GoogleSignInResult()

    data object Cancelled : GoogleSignInResult()
}
