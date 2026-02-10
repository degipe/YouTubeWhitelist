package io.github.degipe.youtubewhitelist.core.auth.google

import java.net.URLEncoder

object OAuthConfig {
    const val AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
    const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
    const val SCOPES = "openid email profile"

    fun buildAuthUrl(clientId: String, state: String, redirectUri: String): String {
        return buildString {
            append(AUTH_ENDPOINT)
            append("?client_id=").append(encode(clientId))
            append("&redirect_uri=").append(encode(redirectUri))
            append("&response_type=code")
            append("&scope=").append(encode(SCOPES))
            append("&state=").append(encode(state))
            append("&access_type=offline")
            append("&prompt=consent")
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
