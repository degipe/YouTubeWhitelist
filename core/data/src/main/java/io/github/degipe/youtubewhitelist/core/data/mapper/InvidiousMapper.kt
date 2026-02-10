package io.github.degipe.youtubewhitelist.core.data.mapper

import io.github.degipe.youtubewhitelist.core.data.model.PlaylistVideo
import io.github.degipe.youtubewhitelist.core.data.model.YouTubeMetadata
import io.github.degipe.youtubewhitelist.core.network.invidious.InvidiousChannelDto
import io.github.degipe.youtubewhitelist.core.network.invidious.InvidiousPlaylistDto
import io.github.degipe.youtubewhitelist.core.network.invidious.InvidiousThumbnail
import io.github.degipe.youtubewhitelist.core.network.invidious.InvidiousVideoDto

object InvidiousMapper {

    fun toVideo(dto: InvidiousVideoDto): YouTubeMetadata.Video = YouTubeMetadata.Video(
        youtubeId = dto.videoId,
        title = dto.title,
        thumbnailUrl = dto.videoThumbnails.bestUrl(dto.videoId),
        channelId = dto.authorId,
        channelTitle = dto.author,
        description = "",
        duration = null
    )

    fun toChannel(dto: InvidiousChannelDto): YouTubeMetadata.Channel = YouTubeMetadata.Channel(
        youtubeId = dto.authorId,
        title = dto.author,
        thumbnailUrl = dto.authorThumbnails.bestUrl(dto.authorId),
        description = "",
        subscriberCount = null,
        videoCount = null,
        uploadsPlaylistId = null
    )

    fun toPlaylist(dto: InvidiousPlaylistDto): YouTubeMetadata.Playlist = YouTubeMetadata.Playlist(
        youtubeId = dto.playlistId,
        title = dto.title,
        thumbnailUrl = dto.playlistThumbnail.ifEmpty {
            dto.videos.firstOrNull()?.let {
                "https://i.ytimg.com/vi/${it.videoId}/hqdefault.jpg"
            } ?: ""
        },
        channelId = dto.authorId,
        channelTitle = dto.author,
        description = ""
    )

    fun channelVideosToPlaylistVideos(dto: InvidiousChannelDto): List<PlaylistVideo> =
        dto.latestVideos.mapIndexed { index, video ->
            PlaylistVideo(
                videoId = video.videoId,
                title = video.title,
                thumbnailUrl = video.videoThumbnails.bestUrl(video.videoId),
                channelTitle = dto.author,
                position = index
            )
        }

    fun playlistVideosToPlaylistVideos(dto: InvidiousPlaylistDto): List<PlaylistVideo> =
        dto.videos.map { video ->
            PlaylistVideo(
                videoId = video.videoId,
                title = video.title,
                thumbnailUrl = "https://i.ytimg.com/vi/${video.videoId}/hqdefault.jpg",
                channelTitle = video.author,
                position = video.index
            )
        }

    private fun List<InvidiousThumbnail>.bestUrl(fallbackId: String): String {
        // Prefer medium quality thumbnail, or fall back to direct YouTube URL
        val medium = find { it.width in 300..500 }
        val any = firstOrNull { it.url.isNotBlank() }
        return medium?.url ?: any?.url ?: "https://i.ytimg.com/vi/$fallbackId/hqdefault.jpg"
    }
}
