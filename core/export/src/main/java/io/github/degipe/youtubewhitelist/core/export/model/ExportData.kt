package io.github.degipe.youtubewhitelist.core.export.model

import kotlinx.serialization.Serializable

@Serializable
data class ExportData(
    val version: Int = 1,
    val exportedAt: Long,
    val profiles: List<ExportProfile>
)

@Serializable
data class ExportProfile(
    val name: String,
    val avatarUrl: String? = null,
    val dailyLimitMinutes: Int? = null,
    val sleepPlaylistId: String? = null,
    val whitelistItems: List<ExportWhitelistItem>
)

@Serializable
data class ExportWhitelistItem(
    val type: String,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val channelTitle: String? = null
)
