package io.github.degipe.youtubewhitelist.core.data.model

data class PlaylistVideo(
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val channelTitle: String,
    val position: Int
)
