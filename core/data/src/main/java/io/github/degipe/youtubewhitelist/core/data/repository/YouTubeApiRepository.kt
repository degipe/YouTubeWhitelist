package io.github.degipe.youtubewhitelist.core.data.repository

import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.model.YouTubeMetadata

interface YouTubeApiRepository {
    suspend fun getChannelById(channelId: String): AppResult<YouTubeMetadata.Channel>
    suspend fun getChannelByHandle(handle: String): AppResult<YouTubeMetadata.Channel>
    suspend fun getVideoById(videoId: String): AppResult<YouTubeMetadata.Video>
    suspend fun getPlaylistById(playlistId: String): AppResult<YouTubeMetadata.Playlist>
}
