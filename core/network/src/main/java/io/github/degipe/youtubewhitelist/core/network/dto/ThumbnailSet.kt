package io.github.degipe.youtubewhitelist.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ThumbnailSet(
    val default: Thumbnail? = null,
    val medium: Thumbnail? = null,
    val high: Thumbnail? = null,
    val standard: Thumbnail? = null,
    val maxres: Thumbnail? = null
)

@Serializable
data class Thumbnail(
    val url: String = "",
    val width: Int = 0,
    val height: Int = 0
)
