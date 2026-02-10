package io.github.degipe.youtubewhitelist.core.auth.google

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import io.github.degipe.youtubewhitelist.core.auth.di.GoogleClientId
import io.github.degipe.youtubewhitelist.core.auth.di.GoogleClientSecret
import java.util.UUID
import javax.inject.Inject

/**
 * Custom Chrome Tabs + loopback server OAuth 2.0 implementation.
 * F-Droid compatible — no Google Play Services SDK dependency.
 *
 * Flow:
 * 1. Starts a local HTTP server on a random port
 * 2. Opens Google OAuth consent screen in a Custom Chrome Tab
 * 3. After user signs in, Google redirects to localhost:{port}/callback
 * 4. Loopback server captures the authorization code
 * 5. Code is exchanged for tokens via HTTP POST to Google token endpoint
 * 6. ID token (JWT) is parsed for user info (email, name)
 */
class GoogleSignInManagerImpl @Inject constructor(
    private val tokenExchanger: OAuthTokenExchanger,
    @GoogleClientId private val clientId: String,
    @GoogleClientSecret private val clientSecret: String
) : GoogleSignInManager {

    private var signedIn = false

    override suspend fun signIn(activityContext: Context): GoogleSignInResult {
        val server = OAuthLoopbackServer()
        try {
            val state = UUID.randomUUID().toString()
            val authUrl = OAuthConfig.buildAuthUrl(clientId, state, server.redirectUri)

            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(activityContext, Uri.parse(authUrl))

            val callbackResult = server.awaitAuthorizationCode()

            // Bring app back to foreground (CCT stays in background)
            bringAppToForeground(activityContext)

            return when (callbackResult) {
                is OAuthCallbackResult.Success -> {
                    try {
                        val tokens = tokenExchanger.exchangeCodeForTokens(
                            callbackResult.code, clientId, clientSecret, server.redirectUri
                        )
                        val userInfo = tokens.idToken?.let { tokenExchanger.parseIdToken(it) }

                        signedIn = true
                        GoogleSignInResult.Success(
                            googleAccountId = userInfo?.sub ?: UUID.randomUUID().toString(),
                            email = userInfo?.email ?: "",
                            displayName = userInfo?.name,
                            accessToken = tokens.accessToken,
                            refreshToken = tokens.refreshToken
                        )
                    } catch (e: Exception) {
                        GoogleSignInResult.Error("Token exchange failed: ${e.message}", e)
                    }
                }
                is OAuthCallbackResult.Error -> {
                    GoogleSignInResult.Error(callbackResult.message)
                }
                is OAuthCallbackResult.Cancelled -> {
                    GoogleSignInResult.Cancelled
                }
            }
        } finally {
            server.shutdown()
        }
    }

    override suspend fun signOut() {
        signedIn = false
    }

    override fun isSignedIn(): Boolean = signedIn

    private fun bringAppToForeground(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (intent != null) {
            context.startActivity(intent)
        }
    }
}
