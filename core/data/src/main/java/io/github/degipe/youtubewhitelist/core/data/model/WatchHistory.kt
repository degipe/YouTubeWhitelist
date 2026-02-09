package io.github.degipe.youtubewhitelist.core.data.model

data class WatchHistory(
    val id: String,
    val kidProfileId: String,
    val videoId: String,
    val videoTitle: String,
    val watchedSeconds: Int,
    val watchedAt: Long
)
