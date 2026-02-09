package io.github.degipe.youtubewhitelist.core.auth.google

import io.github.degipe.youtubewhitelist.core.common.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import javax.inject.Inject

data class OAuthTokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String?,
    val expiresIn: Int
)

data class GoogleUserInfo(
    val sub: String,
    val email: String,
    val name: String?
)

class OAuthTokenExchanger @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun exchangeCodeForTokens(
        code: String,
        clientId: String
    ): OAuthTokenResponse = withContext(ioDispatcher) {
        val url = URL(OAuthConfig.TOKEN_ENDPOINT)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val body = buildString {
                append("code=").append(code)
                append("&client_id=").append(clientId)
                append("&redirect_uri=").append(OAuthConfig.REDIRECT_URI)
                append("&grant_type=authorization_code")
            }

            connection.outputStream.use { it.write(body.toByteArray()) }

            if (connection.responseCode != 200) {
                val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                throw IOException("Token exchange failed (${connection.responseCode}): $errorBody")
            }

            val responseBody = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(responseBody)

            OAuthTokenResponse(
                accessToken = json.getString("access_token"),
                refreshToken = json.optString("refresh_token", null),
                idToken = json.optString("id_token", null),
                expiresIn = json.optInt("expires_in", 3600)
            )
        } finally {
            connection.disconnect()
        }
    }

    fun parseIdToken(idToken: String): GoogleUserInfo {
        val parts = idToken.split(".")
        if (parts.size != 3) throw IllegalArgumentException("Invalid JWT format")

        val payload = parts[1]
        // Add padding if needed for Base64
        val paddedPayload = when (payload.length % 4) {
            2 -> "$payload=="
            3 -> "$payload="
            else -> payload
        }
        val decodedBytes = Base64.getUrlDecoder().decode(paddedPayload)
        val json = JSONObject(String(decodedBytes))

        return GoogleUserInfo(
            sub = json.getString("sub"),
            email = json.getString("email"),
            name = json.optString("name", null)
        )
    }
}
