package io.github.degipe.youtubewhitelist.core.auth.token

interface TokenManager {
    suspend fun saveTokens(accessToken: String, refreshToken: String?, expiresAt: Long)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun isTokenExpired(): Boolean
    suspend fun clearTokens()
}
