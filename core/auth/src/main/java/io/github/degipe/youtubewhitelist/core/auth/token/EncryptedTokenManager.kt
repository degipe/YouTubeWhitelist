package io.github.degipe.youtubewhitelist.core.auth.token

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EncryptedTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenManager {

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            PREFS_NAME,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String?, expiresAt: Long) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    override suspend fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    override suspend fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    override suspend fun isTokenExpired(): Boolean {
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return expiresAt <= System.currentTimeMillis()
    }

    override suspend fun clearTokens() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "auth_tokens_encrypted"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
    }
}
