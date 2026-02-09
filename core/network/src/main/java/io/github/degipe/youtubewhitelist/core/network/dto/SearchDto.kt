package io.github.degipe.youtubewhitelist.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchResultDto(
    val kind: String? = null,
    val id: SearchResultId? = null,
    val snippet: SearchSnippet? = null
)

@Serializable
data class SearchResultId(
    val kind: String? = null,
    val videoId: String? = null,
    val channelId: String? = null,
    val playlistId: String? = null
)

@Serializable
data class SearchSnippet(
    val title: String = "",
    val description: String = "",
    val channelId: String = "",
    val channelTitle: String = "",
    val thumbnails: ThumbnailSet? = null,
    val publishedAt: String? = null
)
