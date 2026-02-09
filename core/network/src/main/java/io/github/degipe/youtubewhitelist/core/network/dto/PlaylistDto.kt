package io.github.degipe.youtubewhitelist.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDto(
    val kind: String? = null,
    val id: String,
    val snippet: PlaylistSnippet? = null
)

@Serializable
data class PlaylistSnippet(
    val title: String = "",
    val description: String = "",
    val channelId: String = "",
    val channelTitle: String = "",
    val thumbnails: ThumbnailSet? = null,
    val publishedAt: String? = null
)

@Serializable
data class PlaylistItemDto(
    val kind: String? = null,
    val snippet: PlaylistItemSnippet? = null
)

@Serializable
data class PlaylistItemSnippet(
    val title: String = "",
    val description: String = "",
    val channelId: String = "",
    val channelTitle: String = "",
    val thumbnails: ThumbnailSet? = null,
    val resourceId: ResourceId? = null,
    val position: Int = 0
)

@Serializable
data class ResourceId(
    val kind: String? = null,
    val videoId: String? = null
)
