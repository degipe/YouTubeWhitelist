package io.github.degipe.youtubewhitelist.core.auth.google

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OAuthConfigTest {

    @Test
    fun `buildAuthUrl contains required parameters`() {
        val url = OAuthConfig.buildAuthUrl("test-client-id", "test-state")

        assertThat(url).contains("client_id=test-client-id")
        assertThat(url).contains("state=test-state")
        assertThat(url).contains("redirect_uri=http%3A%2F%2Flocalhost%2Fcallback")
        assertThat(url).contains("response_type=code")
        assertThat(url).contains("scope=openid")
        assertThat(url).contains("access_type=offline")
        assertThat(url).contains("prompt=consent")
    }

    @Test
    fun `buildAuthUrl starts with Google auth endpoint`() {
        val url = OAuthConfig.buildAuthUrl("client-id", "state")

        assertThat(url).startsWith("https://accounts.google.com/o/oauth2/v2/auth?")
    }
}
