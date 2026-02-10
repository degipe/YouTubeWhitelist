package io.github.degipe.youtubewhitelist.core.data.repository

import io.github.degipe.youtubewhitelist.core.data.model.PlaylistVideo
import kotlinx.coroutines.flow.Flow

interface ChannelVideoCacheRepository {
    fun getVideos(channelId: String): Flow<List<PlaylistVideo>>
    fun searchVideos(channelId: String, query: String): Flow<List<PlaylistVideo>>
    suspend fun cacheVideos(channelId: String, videos: List<PlaylistVideo>)
    suspend fun clearCache(channelId: String)
}
