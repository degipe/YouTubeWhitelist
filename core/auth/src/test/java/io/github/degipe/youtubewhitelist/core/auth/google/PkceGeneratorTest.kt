package io.github.degipe.youtubewhitelist.core.auth.google

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.security.MessageDigest
import java.util.Base64

class PkceGeneratorTest {

    @Test
    fun `challenge is base64url sha256 of verifier`() {
        val pair = PkceGenerator.generate()

        val expectedDigest = MessageDigest.getInstance("SHA-256")
            .digest(pair.verifier.toByteArray(Charsets.US_ASCII))
        val expectedChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(expectedDigest)

        assertThat(pair.challenge).isEqualTo(expectedChallenge)
    }

    @Test
    fun `verifier length is at least 43 characters per RFC 7636`() {
        val pair = PkceGenerator.generate()

        assertThat(pair.verifier.length).isAtLeast(43)
    }

    @Test
    fun `verifier and challenge are url-safe with no padding`() {
        val pair = PkceGenerator.generate()

        assertThat(pair.verifier).doesNotContain("+")
        assertThat(pair.verifier).doesNotContain("/")
        assertThat(pair.verifier).doesNotContain("=")
        assertThat(pair.challenge).doesNotContain("+")
        assertThat(pair.challenge).doesNotContain("/")
        assertThat(pair.challenge).doesNotContain("=")
    }

    @Test
    fun `two generate calls produce different verifiers and challenges`() {
        val first = PkceGenerator.generate()
        val second = PkceGenerator.generate()

        assertThat(first.verifier).isNotEqualTo(second.verifier)
        assertThat(first.challenge).isNotEqualTo(second.challenge)
    }
}
