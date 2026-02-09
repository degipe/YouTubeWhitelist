package io.github.degipe.youtubewhitelist.core.network.api

import io.github.degipe.youtubewhitelist.core.network.dto.ChannelDto
import io.github.degipe.youtubewhitelist.core.network.dto.PlaylistDto
import io.github.degipe.youtubewhitelist.core.network.dto.PlaylistItemDto
import io.github.degipe.youtubewhitelist.core.network.dto.SearchResultDto
import io.github.degipe.youtubewhitelist.core.network.dto.VideoDto
import io.github.degipe.youtubewhitelist.core.network.dto.YouTubeListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("channels")
    suspend fun getChannels(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("id") id: String? = null,
        @Query("forHandle") forHandle: String? = null
    ): Response<YouTubeListResponse<ChannelDto>>

    @GET("videos")
    suspend fun getVideos(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("id") id: String
    ): Response<YouTubeListResponse<VideoDto>>

    @GET("playlists")
    suspend fun getPlaylists(
        @Query("part") part: String = "snippet",
        @Query("id") id: String
    ): Response<YouTubeListResponse<PlaylistDto>>

    @GET("playlistItems")
    suspend fun getPlaylistItems(
        @Query("part") part: String = "snippet",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null
    ): Response<YouTubeListResponse<PlaylistItemDto>>

    @GET("search")
    suspend fun search(
        @Query("part") part: String = "snippet",
        @Query("channelId") channelId: String? = null,
        @Query("q") query: String? = null,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 25,
        @Query("pageToken") pageToken: String? = null
    ): Response<YouTubeListResponse<SearchResultDto>>
}
