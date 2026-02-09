package io.github.degipe.youtubewhitelist.core.common.youtube

import java.net.URI
import java.net.URLDecoder

data class ParsedYouTubeUrl(
    val type: YouTubeContentType,
    val id: String
)

enum class YouTubeContentType {
    VIDEO,
    CHANNEL,
    CHANNEL_HANDLE,
    CHANNEL_CUSTOM,
    PLAYLIST
}

object YouTubeUrlParser {

    private val YOUTUBE_HOSTS = setOf(
        "youtube.com", "www.youtube.com", "m.youtube.com"
    )
    private const val SHORT_HOST = "youtu.be"

    fun parse(url: String): ParsedYouTubeUrl? {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return null

        val uri = try {
            URI(trimmed)
        } catch (_: Exception) {
            return null
        }

        val host = uri.host?.lowercase() ?: return null
        val path = uri.path ?: ""
        val query = uri.query ?: ""

        return when {
            host == SHORT_HOST -> parseShortUrl(path)
            host in YOUTUBE_HOSTS -> parseYouTubeUrl(path, query)
            else -> null
        }
    }

    private fun parseShortUrl(path: String): ParsedYouTubeUrl? {
        val videoId = path.trimStart('/').takeIf { it.isNotBlank() } ?: return null
        return ParsedYouTubeUrl(YouTubeContentType.VIDEO, videoId)
    }

    private fun parseYouTubeUrl(path: String, query: String): ParsedYouTubeUrl? {
        val segments = path.split("/").filter { it.isNotBlank() }

        // Check for playlist in query params (both /playlist and /watch with list=)
        val queryParams = parseQueryParams(query)
        val listParam = queryParams["list"]
        if (listParam != null && listParam.isNotBlank()) {
            // /playlist?list= or /watch?v=...&list=
            if (segments.firstOrNull() == "playlist" || segments.firstOrNull() == "watch") {
                return ParsedYouTubeUrl(YouTubeContentType.PLAYLIST, listParam)
            }
        }

        if (segments.isEmpty()) return null

        return when (segments[0]) {
            "watch" -> {
                val videoId = queryParams["v"]
                if (videoId.isNullOrBlank()) null
                else ParsedYouTubeUrl(YouTubeContentType.VIDEO, videoId)
            }
            "shorts", "embed", "live" -> {
                val videoId = segments.getOrNull(1)?.takeIf { it.isNotBlank() }
                if (videoId != null) ParsedYouTubeUrl(YouTubeContentType.VIDEO, videoId)
                else null
            }
            "channel" -> {
                val channelId = segments.getOrNull(1)?.takeIf { it.isNotBlank() }
                if (channelId != null) ParsedYouTubeUrl(YouTubeContentType.CHANNEL, channelId)
                else null
            }
            "c" -> {
                val customName = segments.getOrNull(1)?.takeIf { it.isNotBlank() }
                if (customName != null) ParsedYouTubeUrl(YouTubeContentType.CHANNEL_CUSTOM, customName)
                else null
            }
            else -> {
                // Check for @handle
                val firstSegment = segments[0]
                if (firstSegment.startsWith("@")) {
                    val handle = firstSegment.substring(1).takeIf { it.isNotBlank() }
                    if (handle != null) ParsedYouTubeUrl(YouTubeContentType.CHANNEL_HANDLE, handle)
                    else null
                } else {
                    null
                }
            }
        }
    }

    private fun parseQueryParams(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()
        return query.split("&").mapNotNull { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2) {
                val key = URLDecoder.decode(parts[0], "UTF-8")
                val value = URLDecoder.decode(parts[1], "UTF-8")
                key to value
            } else null
        }.toMap()
    }
}
