package io.github.degipe.youtubewhitelist.core.data.model

sealed interface YouTubeMetadata {
    val youtubeId: String
    val title: String
    val thumbnailUrl: String

    data class Channel(
        override val youtubeId: String,
        override val title: String,
        override val thumbnailUrl: String,
        val description: String,
        val subscriberCount: String?,
        val videoCount: String?,
        val uploadsPlaylistId: String?
    ) : YouTubeMetadata

    data class Video(
        override val youtubeId: String,
        override val title: String,
        override val thumbnailUrl: String,
        val channelId: String,
        val channelTitle: String,
        val description: String,
        val duration: String?
    ) : YouTubeMetadata

    data class Playlist(
        override val youtubeId: String,
        override val title: String,
        override val thumbnailUrl: String,
        val channelId: String,
        val channelTitle: String,
        val description: String
    ) : YouTubeMetadata
}
