package io.github.degipe.youtubewhitelist.core.data.model

data class WatchStats(
    val totalWatchedSeconds: Int,
    val videosWatchedCount: Int,
    val dailyBreakdown: List<DailyWatchStat>
)

data class DailyWatchStat(
    val dayTimestamp: Long,
    val totalSeconds: Int
)
