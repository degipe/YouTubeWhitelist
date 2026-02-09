package io.github.degipe.youtubewhitelist.core.data.repository.impl

import io.github.degipe.youtubewhitelist.core.common.di.IoDispatcher
import io.github.degipe.youtubewhitelist.core.data.model.DailyWatchStat
import io.github.degipe.youtubewhitelist.core.data.model.WatchHistory
import io.github.degipe.youtubewhitelist.core.data.model.WatchStats
import io.github.degipe.youtubewhitelist.core.data.repository.WatchHistoryRepository
import io.github.degipe.youtubewhitelist.core.database.dao.WatchHistoryDao
import io.github.degipe.youtubewhitelist.core.database.entity.WatchHistoryEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

class WatchHistoryRepositoryImpl @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : WatchHistoryRepository {

    override suspend fun recordWatch(
        profileId: String,
        videoId: String,
        videoTitle: String,
        watchedSeconds: Int
    ) = withContext(ioDispatcher) {
        val entity = WatchHistoryEntity(
            id = UUID.randomUUID().toString(),
            kidProfileId = profileId,
            videoId = videoId,
            videoTitle = videoTitle,
            watchedSeconds = watchedSeconds,
            watchedAt = System.currentTimeMillis()
        )
        watchHistoryDao.insert(entity)
    }

    override fun getRecentHistory(profileId: String, limit: Int): Flow<List<WatchHistory>> {
        return watchHistoryDao.getRecentHistory(profileId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getWatchStats(profileId: String, sinceTimestamp: Long): WatchStats =
        withContext(ioDispatcher) {
            val totalSeconds = watchHistoryDao.getTotalWatchedSeconds(profileId, sinceTimestamp) ?: 0
            val videosCount = watchHistoryDao.getVideosWatchedCount(profileId, sinceTimestamp)
            val dailyAggregates = watchHistoryDao.getDailyWatchTime(profileId, sinceTimestamp)

            WatchStats(
                totalWatchedSeconds = totalSeconds,
                videosWatchedCount = videosCount,
                dailyBreakdown = dailyAggregates.map { aggregate ->
                    DailyWatchStat(
                        dayTimestamp = aggregate.dayTimestamp,
                        totalSeconds = aggregate.totalSeconds
                    )
                }
            )
        }

    override suspend fun getTotalWatchedSecondsToday(profileId: String): Int =
        withContext(ioDispatcher) {
            watchHistoryDao.getTotalWatchedSeconds(profileId, startOfToday()) ?: 0
        }

    override fun getTotalWatchedSecondsTodayFlow(profileId: String): Flow<Int> {
        return watchHistoryDao.getTotalWatchedSecondsFlow(profileId, startOfToday())
    }

    private fun startOfToday(): Long {
        return LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun WatchHistoryEntity.toDomain(): WatchHistory = WatchHistory(
        id = id,
        kidProfileId = kidProfileId,
        videoId = videoId,
        videoTitle = videoTitle,
        watchedSeconds = watchedSeconds,
        watchedAt = watchedAt
    )
}
