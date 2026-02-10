package io.github.degipe.youtubewhitelist.core.network.invidious

import kotlinx.serialization.Serializable

@Serializable
data class InvidiousVideoDto(
    val videoId: String,
    val title: String,
    val author: String = "",
    val authorId: String = "",
    val videoThumbnails: List<InvidiousThumbnail> = emptyList()
)

@Serializable
data class InvidiousChannelDto(
    val authorId: String,
    val author: String,
    val authorThumbnails: List<InvidiousThumbnail> = emptyList(),
    val latestVideos: List<InvidiousVideoDto> = emptyList()
)

@Serializable
data class InvidiousPlaylistDto(
    val playlistId: String,
    val title: String,
    val author: String = "",
    val authorId: String = "",
    val playlistThumbnail: String = "",
    val videos: List<InvidiousPlaylistVideoDto> = emptyList()
)

@Serializable
data class InvidiousPlaylistVideoDto(
    val videoId: String,
    val title: String,
    val author: String = "",
    val index: Int = 0
)

@Serializable
data class InvidiousThumbnail(
    val url: String = "",
    val width: Int = 0,
    val height: Int = 0
)
