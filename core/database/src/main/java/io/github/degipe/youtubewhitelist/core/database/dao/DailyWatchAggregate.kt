package io.github.degipe.youtubewhitelist.core.database.dao

data class DailyWatchAggregate(
    val dayTimestamp: Long,
    val totalSeconds: Int
)
