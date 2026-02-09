package io.github.degipe.youtubewhitelist.core.data.repository

import io.github.degipe.youtubewhitelist.core.data.model.WatchHistory
import io.github.degipe.youtubewhitelist.core.data.model.WatchStats
import kotlinx.coroutines.flow.Flow

interface WatchHistoryRepository {
    suspend fun recordWatch(profileId: String, videoId: String, videoTitle: String, watchedSeconds: Int)
    fun getRecentHistory(profileId: String, limit: Int = 50): Flow<List<WatchHistory>>
    suspend fun getWatchStats(profileId: String, sinceTimestamp: Long): WatchStats
    suspend fun getTotalWatchedSecondsToday(profileId: String): Int
    fun getTotalWatchedSecondsTodayFlow(profileId: String): Flow<Int>
}
