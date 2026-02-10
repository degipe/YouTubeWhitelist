package io.github.degipe.youtubewhitelist.core.network.oembed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OEmbedResponse(
    val title: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_url") val authorUrl: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    val type: String
)
