package io.github.degipe.youtubewhitelist.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.github.degipe.youtubewhitelist.core.database.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Query("SELECT * FROM watch_history WHERE kidProfileId = :profileId ORDER BY watchedAt DESC LIMIT :limit")
    fun getRecentHistory(profileId: String, limit: Int = 50): Flow<List<WatchHistoryEntity>>

    @Query("SELECT SUM(watchedSeconds) FROM watch_history WHERE kidProfileId = :profileId AND watchedAt >= :sinceTimestamp")
    suspend fun getTotalWatchedSeconds(profileId: String, sinceTimestamp: Long): Int?

    @Query("SELECT * FROM watch_history WHERE kidProfileId = :profileId AND watchedAt >= :startOfDay ORDER BY watchedAt DESC")
    fun getTodayHistory(profileId: String, startOfDay: Long): Flow<List<WatchHistoryEntity>>

    @Insert
    suspend fun insert(entry: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE kidProfileId = :profileId")
    suspend fun deleteAllByProfile(profileId: String)

    @Query("DELETE FROM watch_history WHERE watchedAt < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)

    @Query("SELECT COUNT(DISTINCT videoId) FROM watch_history WHERE kidProfileId = :profileId AND watchedAt >= :sinceTimestamp")
    suspend fun getVideosWatchedCount(profileId: String, sinceTimestamp: Long): Int

    @Query("""
        SELECT (watchedAt / 86400000) * 86400000 AS dayTimestamp,
               SUM(watchedSeconds) AS totalSeconds
        FROM watch_history
        WHERE kidProfileId = :profileId AND watchedAt >= :sinceTimestamp
        GROUP BY dayTimestamp
        ORDER BY dayTimestamp ASC
    """)
    suspend fun getDailyWatchTime(profileId: String, sinceTimestamp: Long): List<DailyWatchAggregate>

    @Query("SELECT COALESCE(SUM(watchedSeconds), 0) FROM watch_history WHERE kidProfileId = :profileId AND watchedAt >= :sinceTimestamp")
    fun getTotalWatchedSecondsFlow(profileId: String, sinceTimestamp: Long): Flow<Int>
}
