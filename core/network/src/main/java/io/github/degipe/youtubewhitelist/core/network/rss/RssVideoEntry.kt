package io.github.degipe.youtubewhitelist.core.network.rss

data class RssVideoEntry(
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val channelTitle: String,
    val published: String
)
