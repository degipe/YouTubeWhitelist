package io.github.degipe.youtubewhitelist.core.data.repository.impl

import io.github.degipe.youtubewhitelist.core.common.di.IoDispatcher
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.mapper.InvidiousMapper
import io.github.degipe.youtubewhitelist.core.data.mapper.OEmbedMapper
import io.github.degipe.youtubewhitelist.core.data.model.PlaylistVideo
import io.github.degipe.youtubewhitelist.core.data.model.YouTubeMetadata
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import io.github.degipe.youtubewhitelist.core.network.api.YouTubeApiService
import io.github.degipe.youtubewhitelist.core.network.dto.ThumbnailSet
import io.github.degipe.youtubewhitelist.core.network.invidious.InvidiousApiService
import io.github.degipe.youtubewhitelist.core.network.invidious.InvidiousInstanceManager
import io.github.degipe.youtubewhitelist.core.network.oembed.OEmbedService
import io.github.degipe.youtubewhitelist.core.network.rss.RssFeedParser
import io.github.degipe.youtubewhitelist.core.network.rss.RssVideoEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class HybridYouTubeRepositoryImpl @Inject constructor(
    private val youTubeApiService: YouTubeApiService,
    private val oEmbedService: OEmbedService,
    private val rssFeedParser: RssFeedParser,
    private val invidiousApiService: InvidiousApiService,
    private val invidiousInstanceManager: InvidiousInstanceManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : YouTubeApiRepository {

    override suspend fun getVideoById(videoId: String): AppResult<YouTubeMetadata.Video> =
        withContext(ioDispatcher) {
            // Try 1: oEmbed (free, no quota)
            tryOEmbedVideo(videoId)
                ?: tryApiVideo(videoId)
                ?: tryInvidiousVideo(videoId)
                ?: AppResult.Error("Failed to fetch video metadata from all sources")
        }

    override suspend fun getPlaylistById(playlistId: String): AppResult<YouTubeMetadata.Playlist> =
        withContext(ioDispatcher) {
            // Try 1: oEmbed (free, no quota)
            tryOEmbedPlaylist(playlistId)
                ?: tryApiPlaylist(playlistId)
                ?: tryInvidiousPlaylist(playlistId)
                ?: AppResult.Error("Failed to fetch playlist metadata from all sources")
        }

    override suspend fun getChannelById(channelId: String): AppResult<YouTubeMetadata.Channel> =
        withContext(ioDispatcher) {
            // No oEmbed for channels — go directly to API
            tryApiChannel(channelId)
                ?: tryInvidiousChannel(channelId)
                ?: AppResult.Error("Failed to fetch channel metadata from all sources")
        }

    override suspend fun getChannelByHandle(handle: String): AppResult<YouTubeMetadata.Channel> =
        withContext(ioDispatcher) {
            // No free alternative for handle resolution
            tryApiChannelByHandle(handle)
                ?: tryInvidiousChannelByHandle(handle)
                ?: AppResult.Error("Failed to resolve channel handle from all sources")
        }

    override suspend fun getPlaylistItems(playlistId: String): AppResult<List<PlaylistVideo>> =
        withContext(ioDispatcher) {
            // Try 1: RSS feed (free, max 15 videos — only for channel uploads playlists)
            // RSS only works with channel IDs, not arbitrary playlist IDs.
            // For uploads playlists (UU...), extract the channel ID.
            val channelId = extractChannelIdFromUploadsPlaylist(playlistId)
            val rssResult = if (channelId != null) tryRssFeed(channelId) else null

            rssResult
                ?: tryApiPlaylistItems(playlistId)
                ?: tryInvidiousPlaylistItems(playlistId)
                ?: AppResult.Error("Failed to fetch playlist items from all sources")
        }

    override suspend fun searchVideosInChannel(
        channelId: String,
        query: String
    ): AppResult<List<PlaylistVideo>> = withContext(ioDispatcher) {
        // Search was removed from kid mode; this is kept for API compatibility
        // Only YouTube API supports search — no oEmbed/RSS/Invidious alternative
        tryApiSearch(channelId, query)
            ?: AppResult.Error("Search failed")
    }

    // --- oEmbed ---

    private suspend fun tryOEmbedVideo(videoId: String): AppResult<YouTubeMetadata.Video>? {
        return try {
            val url = "https://www.youtube.com/watch?v=$videoId"
            val response = oEmbedService.getOEmbed(url)
            if (response.isSuccessful) {
                val body = response.body() ?: return null
                AppResult.Success(OEmbedMapper.toVideo(videoId, body))
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun tryOEmbedPlaylist(playlistId: String): AppResult<YouTubeMetadata.Playlist>? {
        return try {
            val url = "https://www.youtube.com/playlist?list=$playlistId"
            val response = oEmbedService.getOEmbed(url)
            if (response.isSuccessful) {
                val body = response.body() ?: return null
                AppResult.Success(OEmbedMapper.toPlaylist(playlistId, body))
            } else null
        } catch (_: Exception) {
            null
        }
    }

    // --- RSS ---

    private suspend fun tryRssFeed(channelId: String): AppResult<List<PlaylistVideo>>? {
        return try {
            val entries = rssFeedParser.fetchChannelVideos(channelId)
            if (entries.isNotEmpty()) {
                AppResult.Success(entries.mapIndexed { index, entry -> entry.toPlaylistVideo(index) })
            } else null
        } catch (_: Exception) {
            null
        }
    }

    // --- YouTube API ---

    private suspend fun tryApiVideo(videoId: String): AppResult<YouTubeMetadata.Video>? {
        return try {
            val response = youTubeApiService.getVideos(id = videoId)
            if (!response.isSuccessful) return null
            val video = response.body()?.items?.firstOrNull() ?: return null
            AppResult.Success(
                YouTubeMetadata.Video(
                    youtubeId = video.id,
                    title = video.snippet?.title ?: "",
                    thumbnailUrl = video.snippet?.thumbnails.bestUrl(),
                    channelId = video.snippet?.channelId ?: "",
                    channelTitle = video.snippet?.channelTitle ?: "",
                    description = video.snippet?.description ?: "",
                    duration = video.contentDetails?.duration
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun tryApiPlaylist(playlistId: String): AppResult<YouTubeMetadata.Playlist>? {
        return try {
            val response = youTubeApiService.getPlaylists(id = playlistId)
            if (!response.isSuccessful) return null
            val playlist = response.body()?.items?.firstOrNull() ?: return null
            AppResult.Success(
                YouTubeMetadata.Playlist(
                    youtubeId = playlist.id,
                    title = playlist.snippet?.title ?: "",
                    thumbnailUrl = playlist.snippet?.thumbnails.bestUrl(),
                    channelId = playlist.snippet?.channelId ?: "",
                    channelTitle = playlist.snippet?.channelTitle ?: "",
                    description = playlist.snippet?.description ?: ""
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun tryApiChannel(channelId: String): AppResult<YouTubeMetadata.Channel>? {
        return try {
            val response = youTubeApiService.getChannels(id = channelId)
            if (!response.isSuccessful) return null
            val channel = response.body()?.items?.firstOrNull() ?: return null
            AppResult.Success(
                YouTubeMetadata.Channel(
                    youtubeId = channel.id,
                    title = channel.snippet?.title ?: "",
                    thumbnailUrl = channel.snippet?.thumbnails.bestUrl(),
                    description = channel.snippet?.description ?: "",
                    subscriberCount = channel.statistics?.subscriberCount,
                    videoCount = channel.statistics?.videoCount,
                    uploadsPlaylistId = channel.contentDetails?.relatedPlaylists?.uploads
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun tryApiChannelByHandle(handle: String): AppResult<YouTubeMetadata.Channel>? {
        return try {
            val response = youTubeApiService.getChannels(forHandle = handle)
            if (!response.isSuccessful) return null
            val channel = response.body()?.items?.firstOrNull() ?: return null
            AppResult.Success(
                YouTubeMetadata.Channel(
                    youtubeId = channel.id,
                    title = channel.snippet?.title ?: "",
                    thumbnailUrl = channel.snippet?.thumbnails.bestUrl(),
                    description = channel.snippet?.description ?: "",
                    subscriberCount = channel.statistics?.subscriberCount,
                    videoCount = channel.statistics?.videoCount,
                    uploadsPlaylistId = channel.contentDetails?.relatedPlaylists?.uploads
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun tryApiPlaylistItems(playlistId: String): AppResult<List<PlaylistVideo>>? {
        return try {
            val response = youTubeApiService.getPlaylistItems(playlistId = playlistId)
            if (!response.isSuccessful) return null
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
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun tryApiSearch(channelId: String, query: String): AppResult<List<PlaylistVideo>>? {
        return try {
            val response = youTubeApiService.search(channelId = channelId, query = query, maxResults = 10)
            if (!response.isSuccessful) return null
            val items = response.body()?.items.orEmpty()
            val videos = items.mapNotNull { item ->
                val videoId = item.id?.videoId ?: return@mapNotNull null
                val snippet = item.snippet ?: return@mapNotNull null
                PlaylistVideo(
                    videoId = videoId,
                    title = snippet.title,
                    thumbnailUrl = snippet.thumbnails.bestUrl(),
                    channelTitle = snippet.channelTitle,
                    position = 0
                )
            }
            AppResult.Success(videos)
        } catch (_: Exception) {
            null
        }
    }

    // --- Invidious ---

    private suspend fun tryInvidiousVideo(videoId: String): AppResult<YouTubeMetadata.Video>? {
        return withInvidiousFallback { baseUrl ->
            val dto = invidiousApiService.getVideo(baseUrl, videoId)
            AppResult.Success(InvidiousMapper.toVideo(dto))
        }
    }

    private suspend fun tryInvidiousPlaylist(playlistId: String): AppResult<YouTubeMetadata.Playlist>? {
        return withInvidiousFallback { baseUrl ->
            val dto = invidiousApiService.getPlaylist(baseUrl, playlistId)
            AppResult.Success(InvidiousMapper.toPlaylist(dto))
        }
    }

    private suspend fun tryInvidiousChannel(channelId: String): AppResult<YouTubeMetadata.Channel>? {
        return withInvidiousFallback { baseUrl ->
            val dto = invidiousApiService.getChannel(baseUrl, channelId)
            AppResult.Success(InvidiousMapper.toChannel(dto))
        }
    }

    private suspend fun tryInvidiousChannelByHandle(handle: String): AppResult<YouTubeMetadata.Channel>? {
        return withInvidiousFallback { baseUrl ->
            val channelId = invidiousApiService.resolveChannel(baseUrl, handle)
            val dto = invidiousApiService.getChannel(baseUrl, channelId)
            AppResult.Success(InvidiousMapper.toChannel(dto))
        }
    }

    private suspend fun tryInvidiousPlaylistItems(playlistId: String): AppResult<List<PlaylistVideo>>? {
        // Check if this is a channel uploads playlist (UU...) — use channel endpoint
        val channelId = extractChannelIdFromUploadsPlaylist(playlistId)
        if (channelId != null) {
            return withInvidiousFallback { baseUrl ->
                val dto = invidiousApiService.getChannel(baseUrl, channelId)
                AppResult.Success(InvidiousMapper.channelVideosToPlaylistVideos(dto))
            }
        }
        // Otherwise, it's a regular playlist
        return withInvidiousFallback { baseUrl ->
            val dto = invidiousApiService.getPlaylist(baseUrl, playlistId)
            AppResult.Success(InvidiousMapper.playlistVideosToPlaylistVideos(dto))
        }
    }

    private suspend fun <T> withInvidiousFallback(
        block: suspend (baseUrl: String) -> AppResult<T>
    ): AppResult<T>? {
        // Try up to 3 instances
        repeat(3) {
            val baseUrl = invidiousInstanceManager.getHealthyInstance() ?: return null
            try {
                val result = block(baseUrl)
                invidiousInstanceManager.reportSuccess(baseUrl)
                return result
            } catch (_: IOException) {
                // Network error — mark instance as unhealthy
                invidiousInstanceManager.reportFailure(baseUrl)
            } catch (_: Exception) {
                // Parsing/data error — don't penalize instance
            }
        }
        return null
    }

    // --- Utilities ---

    private fun extractChannelIdFromUploadsPlaylist(playlistId: String): String? {
        // YouTube uploads playlists have the format "UU" + channelId (without "UC" prefix)
        // e.g., channel UC-yBVzHNBKEx34GBJf8WNQA → uploads playlist UU-yBVzHNBKEx34GBJf8WNQA
        return if (playlistId.startsWith("UU")) {
            "UC${playlistId.removePrefix("UU")}"
        } else null
    }

    private fun RssVideoEntry.toPlaylistVideo(index: Int) = PlaylistVideo(
        videoId = videoId,
        title = title,
        thumbnailUrl = thumbnailUrl,
        channelTitle = channelTitle,
        position = index
    )

    private fun ThumbnailSet?.bestUrl(): String {
        if (this == null) return ""
        return high?.url?.takeIf { it.isNotBlank() }
            ?: medium?.url?.takeIf { it.isNotBlank() }
            ?: default?.url?.takeIf { it.isNotBlank() }
            ?: ""
    }
}
