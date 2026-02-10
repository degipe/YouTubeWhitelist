package io.github.degipe.youtubewhitelist.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.degipe.youtubewhitelist.core.database.entity.CachedChannelVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedChannelVideoDao {

    @Query("SELECT * FROM cached_channel_videos WHERE channelId = :channelId ORDER BY position ASC")
    fun getVideosByChannel(channelId: String): Flow<List<CachedChannelVideoEntity>>

    @Query("SELECT * FROM cached_channel_videos WHERE channelId = :channelId AND title LIKE '%' || :query || '%' ORDER BY position ASC")
    fun searchVideosInChannel(channelId: String, query: String): Flow<List<CachedChannelVideoEntity>>

    @Upsert
    suspend fun upsertAll(videos: List<CachedChannelVideoEntity>)

    @Query("DELETE FROM cached_channel_videos WHERE channelId = :channelId")
    suspend fun deleteByChannel(channelId: String)

    @Query("SELECT MAX(position) FROM cached_channel_videos WHERE channelId = :channelId")
    suspend fun getMaxPosition(channelId: String): Int?
}
