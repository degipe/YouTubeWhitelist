package io.github.degipe.youtubewhitelist.core.auth.google

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class OAuthLoopbackServerTest {

    @Test
    fun `server starts on random port`() {
        val server = OAuthLoopbackServer()
        assertThat(server.port).isGreaterThan(0)
        assertThat(server.redirectUri).isEqualTo("http://localhost:${server.port}/callback")
        server.shutdown()
    }

    @Test
    fun `server captures authorization code from redirect`() = runTest {
        val server = OAuthLoopbackServer()
        val port = server.port

        val resultDeferred = async { server.awaitAuthorizationCode() }
        delay(100) // Let server start listening

        // Simulate browser redirect
        val url = URL("http://localhost:$port/callback?code=test_auth_code&state=abc123")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val responseCode = connection.responseCode
        connection.disconnect()

        val result = resultDeferred.await()

        assertThat(responseCode).isEqualTo(200)
        assertThat(result).isInstanceOf(OAuthCallbackResult.Success::class.java)
        assertThat((result as OAuthCallbackResult.Success).code).isEqualTo("test_auth_code")
    }

    @Test
    fun `server returns error when error parameter present`() = runTest {
        val server = OAuthLoopbackServer()
        val port = server.port

        val resultDeferred = async { server.awaitAuthorizationCode() }
        delay(100)

        val url = URL("http://localhost:$port/callback?error=access_denied")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.responseCode
        connection.disconnect()

        val result = resultDeferred.await()

        assertThat(result).isInstanceOf(OAuthCallbackResult.Error::class.java)
        assertThat((result as OAuthCallbackResult.Error).message).isEqualTo("access_denied")
    }

    @Test
    fun `server returns cancelled when no code or error`() = runTest {
        val server = OAuthLoopbackServer()
        val port = server.port

        val resultDeferred = async { server.awaitAuthorizationCode() }
        delay(100)

        val url = URL("http://localhost:$port/callback")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.responseCode
        connection.disconnect()

        val result = resultDeferred.await()

        assertThat(result).isInstanceOf(OAuthCallbackResult.Cancelled::class.java)
    }

    @Test
    fun `server sends HTML response to browser`() = runTest {
        val server = OAuthLoopbackServer()
        val port = server.port

        val resultDeferred = async { server.awaitAuthorizationCode() }
        delay(100)

        val url = URL("http://localhost:$port/callback?code=test_code")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val body = connection.inputStream.bufferedReader().readText()
        connection.disconnect()

        resultDeferred.await()

        assertThat(body).contains("Sign-in complete")
        assertThat(body).contains("close this tab")
    }

    @Test
    fun `server handles URL-encoded parameters`() = runTest {
        val server = OAuthLoopbackServer()
        val port = server.port

        val resultDeferred = async { server.awaitAuthorizationCode() }
        delay(100)

        val url = URL("http://localhost:$port/callback?code=4%2FaBC%3Ddef&state=test")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.responseCode
        connection.disconnect()

        val result = resultDeferred.await()

        assertThat(result).isInstanceOf(OAuthCallbackResult.Success::class.java)
        assertThat((result as OAuthCallbackResult.Success).code).isEqualTo("4/aBC=def")
    }

    @Test
    fun `shutdown closes server`() {
        val server = OAuthLoopbackServer()
        server.shutdown()
        // Calling shutdown again should not throw
        server.shutdown()
    }
}
