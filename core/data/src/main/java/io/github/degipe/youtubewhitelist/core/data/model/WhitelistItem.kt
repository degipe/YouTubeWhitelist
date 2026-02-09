package io.github.degipe.youtubewhitelist.core.data.model

import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType

data class WhitelistItem(
    val id: String,
    val kidProfileId: String,
    val type: WhitelistItemType,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val channelTitle: String?,
    val addedAt: Long
)
