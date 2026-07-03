package io.github.degipe.youtubewhitelist.feature.kid.ui.player

import com.google.common.truth.Truth.assertThat
import org.junit.Test

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
 */
class VideoPlayerReloadTest {

    @Test
    fun `player html embeds the requested youtubeId`() {
        val html = buildYouTubePlayerHtml(
            videoId = "aaaaaaaaaaa",
            origin = "https://io.github.degipe.youtubewhitelist",
            showControls = true
        )

        assertThat(html).contains("videoId: 'aaaaaaaaaaa'")
    }

    @Test
    fun `player html changes when youtubeId changes`() {
        val origin = "https://io.github.degipe.youtubewhitelist"

        val firstHtml = buildYouTubePlayerHtml("aaaaaaaaaaa", origin, showControls = true)
        val secondHtml = buildYouTubePlayerHtml("bbbbbbbbbbb", origin, showControls = true)

        assertThat(firstHtml).contains("videoId: 'aaaaaaaaaaa'")
        assertThat(firstHtml).doesNotContain("videoId: 'bbbbbbbbbbb'")

        assertThat(secondHtml).contains("videoId: 'bbbbbbbbbbb'")
        assertThat(secondHtml).doesNotContain("videoId: 'aaaaaaaaaaa'")

        assertThat(secondHtml).isNotEqualTo(firstHtml)
    }
}
