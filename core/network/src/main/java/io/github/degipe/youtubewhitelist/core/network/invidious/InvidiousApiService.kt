package io.github.degipe.youtubewhitelist.core.network.invidious

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class InvidiousApiService(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {

    suspend fun getVideo(baseUrl: String, videoId: String): InvidiousVideoDto {
        val url = "$baseUrl/api/v1/videos/$videoId?fields=videoId,title,author,authorId,videoThumbnails"
        val body = fetchJson(url)
        return json.decodeFromString<InvidiousVideoDto>(body)
    }

    suspend fun getChannel(baseUrl: String, channelId: String): InvidiousChannelDto {
        val url = "$baseUrl/api/v1/channels/$channelId?fields=authorId,author,authorThumbnails,latestVideos"
        val body = fetchJson(url)
        return json.decodeFromString<InvidiousChannelDto>(body)
    }

    suspend fun getPlaylist(baseUrl: String, playlistId: String): InvidiousPlaylistDto {
        val url = "$baseUrl/api/v1/playlists/$playlistId"
        val body = fetchJson(url)
        return json.decodeFromString<InvidiousPlaylistDto>(body)
    }

    suspend fun resolveChannel(baseUrl: String, handle: String): String {
        val encodedUrl = java.net.URLEncoder.encode(
            "https://www.youtube.com/@$handle", "UTF-8"
        )
        val url = "$baseUrl/api/v1/resolveurl?url=$encodedUrl"
        val body = fetchJson(url)
        val response = json.decodeFromString<ResolveUrlResponse>(body)
        return response.ucid
    }

    private fun fetchJson(url: String): String {
        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Invidious API error: ${response.code}")
        }
        return response.body?.string() ?: throw IOException("Empty response body")
    }

    @kotlinx.serialization.Serializable
    internal data class ResolveUrlResponse(
        val ucid: String = "",
        val pageType: String = ""
    )
}
