package io.github.degipe.youtubewhitelist.core.auth.google

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Base64

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class OAuthTokenExchangerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val exchanger = OAuthTokenExchanger(testDispatcher)

    @Test
    fun `parseIdToken extracts user info from valid JWT`() {
        val payload = """{"sub":"12345","email":"test@example.com","name":"Test User"}"""
        val encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())
        val jwt = "eyJhbGciOiJSUzI1NiJ9.$encodedPayload.signature"

        val userInfo = exchanger.parseIdToken(jwt)

        assertThat(userInfo.sub).isEqualTo("12345")
        assertThat(userInfo.email).isEqualTo("test@example.com")
        assertThat(userInfo.name).isEqualTo("Test User")
    }

    @Test
    fun `parseIdToken handles null name`() {
        val payload = """{"sub":"67890","email":"user@test.com"}"""
        val encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())
        val jwt = "header.$encodedPayload.signature"

        val userInfo = exchanger.parseIdToken(jwt)

        assertThat(userInfo.sub).isEqualTo("67890")
        assertThat(userInfo.email).isEqualTo("user@test.com")
        assertThat(userInfo.name).isNull()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parseIdToken throws for invalid JWT format`() {
        exchanger.parseIdToken("not-a-jwt")
    }
}
