package io.github.degipe.youtubewhitelist.core.data.repository.impl

import io.github.degipe.youtubewhitelist.core.data.model.PlaylistVideo
import io.github.degipe.youtubewhitelist.core.data.repository.ChannelVideoCacheRepository
import io.github.degipe.youtubewhitelist.core.database.dao.CachedChannelVideoDao
import io.github.degipe.youtubewhitelist.core.database.entity.CachedChannelVideoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChannelVideoCacheRepositoryImpl @Inject constructor(
    private val cachedChannelVideoDao: CachedChannelVideoDao
) : ChannelVideoCacheRepository {

    override fun getVideos(channelId: String): Flow<List<PlaylistVideo>> =
        cachedChannelVideoDao.getVideosByChannel(channelId).map { entities ->
            entities.map { it.toPlaylistVideo() }
        }

    override fun searchVideos(channelId: String, query: String): Flow<List<PlaylistVideo>> =
        cachedChannelVideoDao.searchVideosInChannel(channelId, query).map { entities ->
            entities.map { it.toPlaylistVideo() }
        }

    override suspend fun cacheVideos(channelId: String, videos: List<PlaylistVideo>) {
        val entities = videos.map { it.toEntity(channelId) }
        cachedChannelVideoDao.upsertAll(entities)
    }

    override suspend fun clearCache(channelId: String) {
        cachedChannelVideoDao.deleteByChannel(channelId)
    }

    private fun CachedChannelVideoEntity.toPlaylistVideo() = PlaylistVideo(
        videoId = videoId,
        title = title,
        thumbnailUrl = thumbnailUrl,
        channelTitle = channelTitle,
        position = position
    )

    private fun PlaylistVideo.toEntity(channelId: String) = CachedChannelVideoEntity(
        channelId = channelId,
        videoId = videoId,
        title = title,
        thumbnailUrl = thumbnailUrl,
        channelTitle = channelTitle,
        position = position
    )
}
