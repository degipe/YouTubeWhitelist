package io.github.degipe.youtubewhitelist.core.auth.google

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.github.degipe.youtubewhitelist.core.auth.di.GoogleClientId
import kotlinx.coroutines.CompletableDeferred
import java.util.UUID
import javax.inject.Inject

/**
 * WebView-based OAuth 2.0 implementation of GoogleSignInManager.
 * F-Droid compatible — no Google Play Services SDK dependency.
 *
 * Flow:
 * 1. Launches OAuthActivity (in app module) with Google OAuth consent URL
 * 2. User signs in via WebView
 * 3. OAuthActivity intercepts redirect, returns authorization code
 * 4. Code is exchanged for tokens via HTTP POST to Google token endpoint
 * 5. ID token (JWT) is parsed for user info (email, name)
 */
class GoogleSignInManagerImpl @Inject constructor(
    private val tokenExchanger: OAuthTokenExchanger,
    @GoogleClientId private val clientId: String
) : GoogleSignInManager {

    private var signedIn = false

    override suspend fun signIn(activityContext: Context): GoogleSignInResult {
        val state = UUID.randomUUID().toString()
        val authUrl = OAuthConfig.buildAuthUrl(clientId, state)

        // Launch OAuthActivity and wait for result via static bridge
        val deferred = CompletableDeferred<OAuthCallbackResult>()
        pendingCallback = deferred

        val intent = Intent()
        intent.setClassName(
            activityContext.packageName,
            "io.github.degipe.youtubewhitelist.ui.auth.OAuthActivity"
        )
        intent.putExtra(EXTRA_AUTH_URL, authUrl)

        if (activityContext is Activity) {
            activityContext.startActivityForResult(intent, OAUTH_REQUEST_CODE)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activityContext.startActivity(intent)
        }

        val callbackResult = deferred.await()

        return when (callbackResult) {
            is OAuthCallbackResult.Success -> {
                try {
                    val tokens = tokenExchanger.exchangeCodeForTokens(callbackResult.code, clientId)
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
    }

    override suspend fun signOut() {
        signedIn = false
    }

    override fun isSignedIn(): Boolean = signedIn

    sealed class OAuthCallbackResult {
        data class Success(val code: String) : OAuthCallbackResult()
        data class Error(val message: String) : OAuthCallbackResult()
        data object Cancelled : OAuthCallbackResult()
    }

    companion object {
        const val EXTRA_AUTH_URL = "auth_url"
        const val OAUTH_REQUEST_CODE = 9001

        @Volatile
        var pendingCallback: CompletableDeferred<OAuthCallbackResult>? = null

        /**
         * Called from OAuthActivity when the OAuth flow completes.
         */
        fun onOAuthResult(code: String?, error: String?) {
            val callback = pendingCallback ?: return
            pendingCallback = null

            when {
                code != null -> callback.complete(OAuthCallbackResult.Success(code))
                error != null -> callback.complete(OAuthCallbackResult.Error(error))
                else -> callback.complete(OAuthCallbackResult.Cancelled)
            }
        }
    }
}
