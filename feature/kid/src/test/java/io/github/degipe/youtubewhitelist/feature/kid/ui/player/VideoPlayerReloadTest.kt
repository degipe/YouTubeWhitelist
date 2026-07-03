package io.github.degipe.youtubewhitelist.feature.kid.ui.player

import com.google.common.truth.Truth.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Regression guard for the K1 bug: the player WebView never reloaded when the video
 * changed (Next tap / autoplay / embed-error auto-skip), because [YouTubePlayer]'s
 * `AndroidView.factory` only ran once and `update` was a no-op.
 *
 * The fix wraps the WebView node in `key(youtubeId) { ... }` so Compose tears down and
 * recreates the whole node (including the `factory` lambda) whenever `youtubeId` changes.
 * That recreation always calls [buildYouTubePlayerHtml] with the *current* `youtubeId`.
 *
 * True end-to-end behavior (WebView actually reloading a new video on-screen) requires
 * a running Android WebView and is verified manually on an emulator, not here. This test
 * instead pins down the one pure, framework-free contract the fix depends on: that the
 * generated player HTML for a given youtubeId actually embeds that id. A regression that
 * dropped/ignored the id when the node is rebuilt (e.g. someone re-introducing a cached
 * "first html" value) would be caught by this test failing for the second id.
 *
 * Note: [buildYouTubePlayerHtml] uses `org.json.JSONObject.quote` (Task 2.1, bug B1) which
 * is a stub on the plain JVM `android.jar` - Robolectric is required to run these tests.
 */
@RunWith(RobolectricTestRunner::class)
class VideoPlayerReloadTest {

    @Test
    fun `player html embeds the requested youtubeId`() {
        val html = buildYouTubePlayerHtml(
            videoId = "aaaaaaaaaaa",
            origin = "https://io.github.degipe.youtubewhitelist",
            showControls = true
        )

        assertThat(html).contains("videoId: ${JSONObject.quote("aaaaaaaaaaa")},")
    }

    @Test
    fun `player html changes when youtubeId changes`() {
        val origin = "https://io.github.degipe.youtubewhitelist"

        val firstHtml = buildYouTubePlayerHtml("aaaaaaaaaaa", origin, showControls = true)
        val secondHtml = buildYouTubePlayerHtml("bbbbbbbbbbb", origin, showControls = true)

        assertThat(firstHtml).contains("videoId: ${JSONObject.quote("aaaaaaaaaaa")},")
        assertThat(firstHtml).doesNotContain("videoId: ${JSONObject.quote("bbbbbbbbbbb")},")

        assertThat(secondHtml).contains("videoId: ${JSONObject.quote("bbbbbbbbbbb")},")
        assertThat(secondHtml).doesNotContain("videoId: ${JSONObject.quote("aaaaaaaaaaa")},")

        assertThat(secondHtml).isNotEqualTo(firstHtml)
    }

    // === B1: JS-injection hole - the id must be JSON-encoded, never raw-interpolated ===

    @Test
    fun `player html JSON-encodes a malicious id instead of breaking out of the JS literal`() {
        val payload = "');alert(1)//"
        val html = buildYouTubePlayerHtml(
            videoId = payload,
            origin = "https://io.github.degipe.youtubewhitelist",
            showControls = true
        )

        // The safe, JSON-encoded form must be present verbatim.
        assertThat(html).contains("videoId: ${JSONObject.quote(payload)},")

        // The old vulnerable form (raw payload inside a single-quoted JS string, which the
        // payload's leading `'` would close) must never appear.
        assertThat(html).doesNotContain("videoId: '$payload'")
    }

    @Test
    fun `player html escapes embedded quotes and backslashes in the id`() {
        val payload = "\"\\;alert(1)"
        val html = buildYouTubePlayerHtml(
            videoId = payload,
            origin = "https://io.github.degipe.youtubewhitelist",
            showControls = true
        )

        val safeId = JSONObject.quote(payload)
        // Sanity check on the fixture itself: JSONObject.quote must actually escape the
        // dangerous characters, otherwise this test would prove nothing.
        assertThat(safeId).isEqualTo("\"\\\"\\\\;alert(1)\"")

        assertThat(html).contains("videoId: $safeId,")
        // The raw, un-escaped payload must never appear inside the JS literal.
        assertThat(html).doesNotContain("videoId: \"$payload\",")
    }
}
