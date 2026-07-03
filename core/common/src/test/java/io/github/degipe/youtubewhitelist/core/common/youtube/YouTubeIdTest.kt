package io.github.degipe.youtubewhitelist.core.common.youtube

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Validates the strict whitelist regexes for YouTube video/channel/playlist IDs.
 *
 * These validators close bug B1: an unvalidated YouTube ID (e.g. from a crafted import
 * .json) was string-interpolated into the kid player's WebView JavaScript, letting a
 * payload like `');alert(1)//` break out of the JS string literal and execute arbitrary
 * JS in the player WebView. Rejecting malformed IDs before they ever reach the parser,
 * the importer, or the player HTML template closes the hole at the source.
 */
class YouTubeIdTest {

    // === Video ===

    @Test
    fun `valid 11-char video id`() {
        assertThat(YouTubeId.isValidVideoId("dQw4w9WgXcQ")).isTrue()
    }

    @Test
    fun `valid video id with hyphen and underscore`() {
        assertThat(YouTubeId.isValidVideoId("a-B_c1D2e3F")).isTrue()
    }

    @Test
    fun `injection payload rejected`() {
        assertThat(YouTubeId.isValidVideoId("');alert(1)//")).isFalse()
    }

    @Test
    fun `video id too short is rejected`() {
        assertThat(YouTubeId.isValidVideoId("shortId")).isFalse()
    }

    @Test
    fun `video id too long is rejected`() {
        assertThat(YouTubeId.isValidVideoId("dQw4w9WgXcQextra")).isFalse()
    }

    @Test
    fun `video id with disallowed characters is rejected`() {
        assertThat(YouTubeId.isValidVideoId("dQw4w9WgX!Q")).isFalse()
    }

    @Test
    fun `blank video id is rejected`() {
        assertThat(YouTubeId.isValidVideoId("")).isFalse()
    }

    // === Channel ===

    @Test
    fun `channel id must start UC and be 24 chars`() {
        assertThat(YouTubeId.isValidChannelId("UCX6OQ3DkcsbYNE6H8uQQuVA")).isTrue()
        assertThat(YouTubeId.isValidChannelId("bad")).isFalse()
    }

    @Test
    fun `channel id without UC prefix is rejected`() {
        assertThat(YouTubeId.isValidChannelId("XCX6OQ3DkcsbYNE6H8uQQuVA")).isFalse()
    }

    @Test
    fun `channel injection payload rejected`() {
        assertThat(YouTubeId.isValidChannelId("UC');alert(1)//aaaaaaaaa")).isFalse()
    }

    // === Playlist ===

    @Test
    fun `valid playlist id with PL prefix`() {
        assertThat(YouTubeId.isValidPlaylistId("PL1234567890")).isTrue()
    }

    @Test
    fun `valid playlist id with UU prefix`() {
        assertThat(YouTubeId.isValidPlaylistId("UU1234567890")).isTrue()
    }

    @Test
    fun `playlist id too short is rejected`() {
        assertThat(YouTubeId.isValidPlaylistId("PL789")).isFalse()
    }

    @Test
    fun `playlist injection payload rejected`() {
        assertThat(YouTubeId.isValidPlaylistId("PL');alert(1)//")).isFalse()
    }
}
