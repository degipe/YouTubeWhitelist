package io.github.degipe.youtubewhitelist.core.common.youtube

/**
 * Strict whitelist validators for YouTube video/channel/playlist IDs.
 *
 * These IDs are eventually string-interpolated into the kid player's WebView JavaScript
 * (see `buildYouTubePlayerHtml`) and are also accepted from untrusted sources such as
 * imported backup `.json` files. Validating them against YouTube's known ID formats
 * before they are parsed, persisted, or rendered prevents a crafted value (e.g.
 * `');alert(1)//`) from breaking out of a JS string literal and executing arbitrary
 * script inside the kid-mode player (bug B1).
 */
object YouTubeId {
    private val VIDEO = Regex("^[A-Za-z0-9_-]{11}$")
    private val CHANNEL = Regex("^UC[A-Za-z0-9_-]{22}$")
    private val PLAYLIST = Regex("^(PL|UU|FL|LL|RD)[A-Za-z0-9_-]{10,}$")

    fun isValidVideoId(s: String) = VIDEO.matches(s)
    fun isValidChannelId(s: String) = CHANNEL.matches(s)
    fun isValidPlaylistId(s: String) = PLAYLIST.matches(s)
}
