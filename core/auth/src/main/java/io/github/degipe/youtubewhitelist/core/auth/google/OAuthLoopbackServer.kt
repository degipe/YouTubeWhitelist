package io.github.degipe.youtubewhitelist.core.auth.google

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.SocketTimeoutException
import java.net.URLDecoder

/**
 * Lightweight loopback HTTP server for capturing OAuth 2.0 redirect callbacks.
 *
 * Starts a [ServerSocket] on a random available port, waits for the browser
 * to redirect to `http://localhost:{port}/callback?code=...`, extracts the
 * authorization code, sends a "you can close this tab" response, and returns
 * the result.
 *
 * Per RFC 8252 §7.3, Google's authorization server allows any port for
 * loopback IP redirect URIs, so this works with the registered
 * `http://localhost/callback` redirect URI.
 */
class OAuthLoopbackServer {

    private val serverSocket: ServerSocket = ServerSocket(0)

    val port: Int = serverSocket.localPort
    val redirectUri: String = "http://localhost:$port/callback"

    suspend fun awaitAuthorizationCode(): OAuthCallbackResult = withContext(Dispatchers.IO) {
        try {
            serverSocket.soTimeout = TIMEOUT_MS
            val socket = serverSocket.accept()
            try {
                val reader = socket.getInputStream().bufferedReader()
                val requestLine = reader.readLine() ?: return@withContext OAuthCallbackResult.Error("Empty request")

                // Parse "GET /callback?code=xxx&state=yyy HTTP/1.1"
                val parts = requestLine.split(" ")
                if (parts.size < 2) return@withContext OAuthCallbackResult.Error("Invalid HTTP request")

                val params = parseQueryParams(parts[1])
                val code = params["code"]
                val error = params["error"]

                // Send response to browser
                val responseBody = RESPONSE_HTML
                val response = buildString {
                    append("HTTP/1.1 200 OK\r\n")
                    append("Content-Type: text/html; charset=utf-8\r\n")
                    append("Content-Length: ${responseBody.toByteArray().size}\r\n")
                    append("Connection: close\r\n")
                    append("\r\n")
                    append(responseBody)
                }
                socket.getOutputStream().write(response.toByteArray())
                socket.getOutputStream().flush()

                when {
                    code != null -> OAuthCallbackResult.Success(code)
                    error != null -> OAuthCallbackResult.Error(error)
                    else -> OAuthCallbackResult.Cancelled
                }
            } finally {
                socket.close()
            }
        } catch (_: SocketTimeoutException) {
            OAuthCallbackResult.Cancelled
        } catch (e: Exception) {
            OAuthCallbackResult.Error("OAuth callback failed: ${e.message}")
        } finally {
            shutdown()
        }
    }

    fun shutdown() {
        try {
            if (!serverSocket.isClosed) serverSocket.close()
        } catch (_: Exception) {
        }
    }

    private fun parseQueryParams(uri: String): Map<String, String> {
        val queryString = uri.substringAfter("?", "")
        if (queryString.isEmpty()) return emptyMap()
        return queryString.split("&").mapNotNull { param ->
            val eqIndex = param.indexOf('=')
            if (eqIndex > 0) {
                val key = param.substring(0, eqIndex)
                val value = URLDecoder.decode(param.substring(eqIndex + 1), "UTF-8")
                key to value
            } else null
        }.toMap()
    }

    companion object {
        private const val TIMEOUT_MS = 300_000 // 5 minutes
        private const val RESPONSE_HTML =
            "<html><body style=\"font-family:sans-serif;text-align:center;padding:40px\">" +
                    "<h2>Sign-in complete</h2>" +
                    "<p>You can close this tab and return to the app.</p>" +
                    "</body></html>"
    }
}

sealed class OAuthCallbackResult {
    data class Success(val code: String) : OAuthCallbackResult()
    data class Error(val message: String) : OAuthCallbackResult()
    data object Cancelled : OAuthCallbackResult()
}
