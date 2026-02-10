package io.github.degipe.youtubewhitelist.core.data.mapper

import io.github.degipe.youtubewhitelist.core.data.model.YouTubeMetadata
import io.github.degipe.youtubewhitelist.core.network.oembed.OEmbedResponse

object OEmbedMapper {

    fun toVideo(videoId: String, response: OEmbedResponse): YouTubeMetadata.Video {
        val channelId = extractChannelId(response.authorUrl)
        return YouTubeMetadata.Video(
            youtubeId = videoId,
            title = response.title,
            thumbnailUrl = response.thumbnailUrl,
            channelId = channelId,
            channelTitle = response.authorName,
            description = "",
            duration = null
        )
    }

    fun toPlaylist(playlistId: String, response: OEmbedResponse): YouTubeMetadata.Playlist {
        val channelId = extractChannelId(response.authorUrl)
        return YouTubeMetadata.Playlist(
            youtubeId = playlistId,
            title = response.title,
            thumbnailUrl = response.thumbnailUrl,
            channelId = channelId,
            channelTitle = response.authorName,
            description = ""
        )
    }

    private fun extractChannelId(authorUrl: String): String {
        // author_url format: https://www.youtube.com/channel/UC...
        return authorUrl.substringAfterLast("/channel/", "")
    }
}
