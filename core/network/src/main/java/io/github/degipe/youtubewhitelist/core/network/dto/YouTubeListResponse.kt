package io.github.degipe.youtubewhitelist.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class YouTubeListResponse<T>(
    val kind: String? = null,
    val etag: String? = null,
    val pageInfo: PageInfo? = null,
    val nextPageToken: String? = null,
    val prevPageToken: String? = null,
    val items: List<T> = emptyList()
)

@Serializable
data class PageInfo(
    val totalResults: Int = 0,
    val resultsPerPage: Int = 0
)
