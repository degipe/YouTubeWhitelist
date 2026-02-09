package io.github.degipe.youtubewhitelist.ui.auth

import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import io.github.degipe.youtubewhitelist.core.auth.google.GoogleSignInManagerImpl
import io.github.degipe.youtubewhitelist.core.auth.google.OAuthConfig

class OAuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authUrl = intent.getStringExtra(GoogleSignInManagerImpl.EXTRA_AUTH_URL) ?: run {
            GoogleSignInManagerImpl.onOAuthResult(null, "No auth URL provided")
            finish()
            return
        }

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    val url = request.url.toString()
                    if (url.startsWith(OAuthConfig.REDIRECT_URI)) {
                        handleRedirect(request.url)
                        return true
                    }
                    return false
                }
            }
        }

        setContentView(webView)
        webView.loadUrl(authUrl)
    }

    private fun handleRedirect(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val error = uri.getQueryParameter("error")
        GoogleSignInManagerImpl.onOAuthResult(code, error)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // User cancelled OAuth flow
        GoogleSignInManagerImpl.onOAuthResult(null, null)
    }
}
