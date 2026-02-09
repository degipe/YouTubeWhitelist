package io.github.degipe.youtubewhitelist.core.network.dto

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class YouTubeDtoDeserializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Test
    fun `deserialize channel response`() {
        val jsonString = """
        {
          "kind": "youtube#channelListResponse",
          "pageInfo": { "totalResults": 1, "resultsPerPage": 5 },
          "items": [{
            "kind": "youtube#channel",
            "id": "UCxxxxxxxxxxxxxxxxxxxxxx",
            "snippet": {
              "title": "Test Channel",
              "description": "A test channel",
              "customUrl": "@testchannel",
              "thumbnails": {
                "default": { "url": "https://img.youtube.com/default.jpg", "width": 88, "height": 88 },
                "medium": { "url": "https://img.youtube.com/medium.jpg", "width": 240, "height": 240 },
                "high": { "url": "https://img.youtube.com/high.jpg", "width": 800, "height": 800 }
              },
              "publishedAt": "2020-01-01T00:00:00Z"
            },
            "contentDetails": {
              "relatedPlaylists": { "uploads": "UUxxxxxxxxxxxxxxxxxxxxxx" }
            },
            "statistics": {
              "viewCount": "1000000",
              "subscriberCount": "50000",
              "videoCount": "100"
            }
          }]
        }
        """.trimIndent()

        val response = json.decodeFromString<YouTubeListResponse<ChannelDto>>(jsonString)
        assertThat(response.items).hasSize(1)

        val channel = response.items[0]
        assertThat(channel.id).isEqualTo("UCxxxxxxxxxxxxxxxxxxxxxx")
        assertThat(channel.snippet?.title).isEqualTo("Test Channel")
        assertThat(channel.snippet?.description).isEqualTo("A test channel")
        assertThat(channel.snippet?.customUrl).isEqualTo("@testchannel")
        assertThat(channel.snippet?.thumbnails?.high?.url).isEqualTo("https://img.youtube.com/high.jpg")
        assertThat(channel.contentDetails?.relatedPlaylists?.uploads).isEqualTo("UUxxxxxxxxxxxxxxxxxxxxxx")
        assertThat(channel.statistics?.subscriberCount).isEqualTo("50000")
        assertThat(channel.statistics?.videoCount).isEqualTo("100")
    }

    @Test
    fun `deserialize video response`() {
        val jsonString = """
        {
          "kind": "youtube#videoListResponse",
          "items": [{
            "kind": "youtube#video",
            "id": "dQw4w9WgXcQ",
            "snippet": {
              "title": "Test Video",
              "description": "A test video",
              "channelId": "UCxxxxxx",
              "channelTitle": "Test Channel",
              "thumbnails": {
                "default": { "url": "https://i.ytimg.com/vi/dQw4w9WgXcQ/default.jpg" },
                "high": { "url": "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg" }
              },
              "publishedAt": "2023-06-15T10:00:00Z"
            },
            "contentDetails": {
              "duration": "PT4M33S"
            }
          }]
        }
        """.trimIndent()

        val response = json.decodeFromString<YouTubeListResponse<VideoDto>>(jsonString)
        assertThat(response.items).hasSize(1)

        val video = response.items[0]
        assertThat(video.id).isEqualTo("dQw4w9WgXcQ")
        assertThat(video.snippet?.title).isEqualTo("Test Video")
        assertThat(video.snippet?.channelId).isEqualTo("UCxxxxxx")
        assertThat(video.snippet?.channelTitle).isEqualTo("Test Channel")
        assertThat(video.contentDetails?.duration).isEqualTo("PT4M33S")
    }

    @Test
    fun `deserialize playlist response`() {
        val jsonString = """
        {
          "kind": "youtube#playlistListResponse",
          "items": [{
            "kind": "youtube#playlist",
            "id": "PLxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
            "snippet": {
              "title": "Test Playlist",
              "description": "A test playlist",
              "channelId": "UCxxxxxx",
              "channelTitle": "Test Channel",
              "thumbnails": {
                "medium": { "url": "https://i.ytimg.com/vi/medium.jpg" }
              }
            }
          }]
        }
        """.trimIndent()

        val response = json.decodeFromString<YouTubeListResponse<PlaylistDto>>(jsonString)
        assertThat(response.items).hasSize(1)

        val playlist = response.items[0]
        assertThat(playlist.id).isEqualTo("PLxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
        assertThat(playlist.snippet?.title).isEqualTo("Test Playlist")
        assertThat(playlist.snippet?.channelTitle).isEqualTo("Test Channel")
    }

    @Test
    fun `deserialize playlist items response`() {
        val jsonString = """
        {
          "items": [{
            "kind": "youtube#playlistItem",
            "snippet": {
              "title": "Video in Playlist",
              "channelId": "UCxxxxxx",
              "channelTitle": "Test Channel",
              "resourceId": {
                "kind": "youtube#video",
                "videoId": "abc123"
              },
              "position": 0
            }
          }]
        }
        """.trimIndent()

        val response = json.decodeFromString<YouTubeListResponse<PlaylistItemDto>>(jsonString)
        assertThat(response.items).hasSize(1)

        val item = response.items[0]
        assertThat(item.snippet?.title).isEqualTo("Video in Playlist")
        assertThat(item.snippet?.resourceId?.videoId).isEqualTo("abc123")
        assertThat(item.snippet?.position).isEqualTo(0)
    }

    @Test
    fun `deserialize search response`() {
        val jsonString = """
        {
          "items": [{
            "kind": "youtube#searchResult",
            "id": {
              "kind": "youtube#video",
              "videoId": "search123"
            },
            "snippet": {
              "title": "Search Result",
              "channelId": "UCxxxxxx",
              "channelTitle": "Test Channel",
              "thumbnails": {
                "default": { "url": "https://i.ytimg.com/vi/search123/default.jpg" }
              }
            }
          }]
        }
        """.trimIndent()

        val response = json.decodeFromString<YouTubeListResponse<SearchResultDto>>(jsonString)
        assertThat(response.items).hasSize(1)

        val result = response.items[0]
        assertThat(result.id?.videoId).isEqualTo("search123")
        assertThat(result.snippet?.title).isEqualTo("Search Result")
    }

    @Test
    fun `deserialize handles missing optional fields`() {
        val jsonString = """
        {
          "items": [{
            "id": "UCxxxxxx",
            "snippet": {
              "title": "Minimal Channel"
            }
          }]
        }
        """.trimIndent()

        val response = json.decodeFromString<YouTubeListResponse<ChannelDto>>(jsonString)
        assertThat(response.items).hasSize(1)

        val channel = response.items[0]
        assertThat(channel.id).isEqualTo("UCxxxxxx")
        assertThat(channel.snippet?.title).isEqualTo("Minimal Channel")
        assertThat(channel.snippet?.description).isEqualTo("")
        assertThat(channel.snippet?.customUrl).isNull()
        assertThat(channel.snippet?.thumbnails).isNull()
        assertThat(channel.contentDetails).isNull()
        assertThat(channel.statistics).isNull()
        assertThat(response.kind).isNull()
        assertThat(response.pageInfo).isNull()
    }

    @Test
    fun `deserialize handles empty items list`() {
        val jsonString = """
        {
          "kind": "youtube#channelListResponse",
          "pageInfo": { "totalResults": 0, "resultsPerPage": 5 },
          "items": []
        }
        """.trimIndent()

        val response = json.decodeFromString<YouTubeListResponse<ChannelDto>>(jsonString)
        assertThat(response.items).isEmpty()
        assertThat(response.pageInfo?.totalResults).isEqualTo(0)
    }

    @Test
    fun `deserialize ignores unknown keys`() {
        val jsonString = """
        {
          "items": [{
            "id": "UCxxxxxx",
            "snippet": {
              "title": "Channel",
              "unknownField": "should be ignored"
            },
            "unknownTopLevel": true
          }]
        }
        """.trimIndent()

        val response = json.decodeFromString<YouTubeListResponse<ChannelDto>>(jsonString)
        assertThat(response.items).hasSize(1)
        assertThat(response.items[0].snippet?.title).isEqualTo("Channel")
    }

    @Test
    fun `deserialize thumbnail set with all sizes`() {
        val jsonString = """
        {
          "default": { "url": "https://img/default.jpg", "width": 120, "height": 90 },
          "medium": { "url": "https://img/medium.jpg", "width": 320, "height": 180 },
          "high": { "url": "https://img/high.jpg", "width": 480, "height": 360 },
          "standard": { "url": "https://img/standard.jpg", "width": 640, "height": 480 },
          "maxres": { "url": "https://img/maxres.jpg", "width": 1280, "height": 720 }
        }
        """.trimIndent()

        val thumbnails = json.decodeFromString<ThumbnailSet>(jsonString)
        assertThat(thumbnails.default?.url).isEqualTo("https://img/default.jpg")
        assertThat(thumbnails.medium?.url).isEqualTo("https://img/medium.jpg")
        assertThat(thumbnails.high?.url).isEqualTo("https://img/high.jpg")
        assertThat(thumbnails.standard?.url).isEqualTo("https://img/standard.jpg")
        assertThat(thumbnails.maxres?.url).isEqualTo("https://img/maxres.jpg")
        assertThat(thumbnails.maxres?.width).isEqualTo(1280)
    }
}
