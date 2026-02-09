package io.github.degipe.youtubewhitelist.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class VideoDto(
    val kind: String? = null,
    val id: String,
    val snippet: VideoSnippet? = null,
    val contentDetails: VideoContentDetails? = null
)

@Serializable
data class VideoSnippet(
    val title: String = "",
    val description: String = "",
    val channelId: String = "",
    val channelTitle: String = "",
    val thumbnails: ThumbnailSet? = null,
    val publishedAt: String? = null
)

@Serializable
data class VideoContentDetails(
    val duration: String? = null
)
