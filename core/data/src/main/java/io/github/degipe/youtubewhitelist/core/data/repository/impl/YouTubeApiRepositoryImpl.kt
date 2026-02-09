package io.github.degipe.youtubewhitelist.core.data.repository.impl

import io.github.degipe.youtubewhitelist.core.common.di.IoDispatcher
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.model.PlaylistVideo
import io.github.degipe.youtubewhitelist.core.data.model.YouTubeMetadata
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import io.github.degipe.youtubewhitelist.core.network.api.YouTubeApiService
import io.github.degipe.youtubewhitelist.core.network.dto.ChannelDto
import io.github.degipe.youtubewhitelist.core.network.dto.PlaylistDto
import io.github.degipe.youtubewhitelist.core.network.dto.ThumbnailSet
import io.github.degipe.youtubewhitelist.core.network.dto.VideoDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class YouTubeApiRepositoryImpl @Inject constructor(
    private val youTubeApiService: YouTubeApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : YouTubeApiRepository {

    override suspend fun getChannelById(channelId: String): AppResult<YouTubeMetadata.Channel> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = youTubeApiService.getChannels(id = channelId)
                if (!response.isSuccessful) {
                    return@safeApiCall AppResult.Error("API error: ${response.code()}")
                }
                val channel = response.body()?.items?.firstOrNull()
                    ?: return@safeApiCall AppResult.Error("Channel not found")
                AppResult.Success(channel.toDomain())
            }
        }

    override suspend fun getChannelByHandle(handle: String): AppResult<YouTubeMetadata.Channel> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = youTubeApiService.getChannels(forHandle = handle)
                if (!response.isSuccessful) {
                    return@safeApiCall AppResult.Error("API error: ${response.code()}")
                }
                val channel = response.body()?.items?.firstOrNull()
                    ?: return@safeApiCall AppResult.Error("Channel not found for handle: @$handle")
                AppResult.Success(channel.toDomain())
            }
        }

    override suspend fun getVideoById(videoId: String): AppResult<YouTubeMetadata.Video> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = youTubeApiService.getVideos(id = videoId)
                if (!response.isSuccessful) {
                    return@safeApiCall AppResult.Error("API error: ${response.code()}")
                }
                val video = response.body()?.items?.firstOrNull()
                    ?: return@safeApiCall AppResult.Error("Video not found")
                AppResult.Success(video.toDomain())
            }
        }

    override suspend fun getPlaylistById(playlistId: String): AppResult<YouTubeMetadata.Playlist> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = youTubeApiService.getPlaylists(id = playlistId)
                if (!response.isSuccessful) {
                    return@safeApiCall AppResult.Error("API error: ${response.code()}")
                }
                val playlist = response.body()?.items?.firstOrNull()
                    ?: return@safeApiCall AppResult.Error("Playlist not found")
                AppResult.Success(playlist.toDomain())
            }
        }

    override suspend fun getPlaylistItems(playlistId: String): AppResult<List<PlaylistVideo>> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = youTubeApiService.getPlaylistItems(playlistId = playlistId)
                if (!response.isSuccessful) {
                    return@safeApiCall AppResult.Error("API error: ${response.code()}")
                }
                val items = response.body()?.items.orEmpty()
                val videos = items.mapNotNull { item ->
                    val snippet = item.snippet ?: return@mapNotNull null
                    val videoId = snippet.resourceId?.videoId ?: return@mapNotNull null
                    PlaylistVideo(
                        videoId = videoId,
                        title = snippet.title,
                        thumbnailUrl = snippet.thumbnails.bestUrl(),
                        channelTitle = snippet.channelTitle,
                        position = snippet.position
                    )
                }
                AppResult.Success(videos)
            }
        }

    private suspend fun <T> safeApiCall(block: suspend () -> AppResult<T>): AppResult<T> {
        return try {
            block()
        } catch (e: IOException) {
            AppResult.Error("Network error", e)
        } catch (e: Exception) {
            AppResult.Error("Unexpected error", e)
        }
    }

    companion object {
        fun ChannelDto.toDomain(): YouTubeMetadata.Channel = YouTubeMetadata.Channel(
            youtubeId = id,
            title = snippet?.title ?: "",
            thumbnailUrl = snippet?.thumbnails.bestUrl(),
            description = snippet?.description ?: "",
            subscriberCount = statistics?.subscriberCount,
            videoCount = statistics?.videoCount,
            uploadsPlaylistId = contentDetails?.relatedPlaylists?.uploads
        )

        fun VideoDto.toDomain(): YouTubeMetadata.Video = YouTubeMetadata.Video(
            youtubeId = id,
            title = snippet?.title ?: "",
            thumbnailUrl = snippet?.thumbnails.bestUrl(),
            channelId = snippet?.channelId ?: "",
            channelTitle = snippet?.channelTitle ?: "",
            description = snippet?.description ?: "",
            duration = contentDetails?.duration
        )

        fun PlaylistDto.toDomain(): YouTubeMetadata.Playlist = YouTubeMetadata.Playlist(
            youtubeId = id,
            title = snippet?.title ?: "",
            thumbnailUrl = snippet?.thumbnails.bestUrl(),
            channelId = snippet?.channelId ?: "",
            channelTitle = snippet?.channelTitle ?: "",
            description = snippet?.description ?: ""
        )

        fun ThumbnailSet?.bestUrl(): String {
            if (this == null) return ""
            return high?.url?.takeIf { it.isNotBlank() }
                ?: medium?.url?.takeIf { it.isNotBlank() }
                ?: default?.url?.takeIf { it.isNotBlank() }
                ?: ""
        }
    }
}
