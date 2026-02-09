package io.github.degipe.youtubewhitelist.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChannelDto(
    val kind: String? = null,
    val id: String,
    val snippet: ChannelSnippet? = null,
    val contentDetails: ChannelContentDetails? = null,
    val statistics: ChannelStatistics? = null
)

@Serializable
data class ChannelSnippet(
    val title: String = "",
    val description: String = "",
    val customUrl: String? = null,
    val thumbnails: ThumbnailSet? = null,
    val publishedAt: String? = null
)

@Serializable
data class ChannelContentDetails(
    val relatedPlaylists: RelatedPlaylists? = null
)

@Serializable
data class RelatedPlaylists(
    val likes: String? = null,
    val uploads: String? = null
)

@Serializable
data class ChannelStatistics(
    val viewCount: String? = null,
    val subscriberCount: String? = null,
    val videoCount: String? = null
)
