package io.github.degipe.youtubewhitelist.core.network.oembed

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class OEmbedResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserialize video oEmbed response`() {
        val jsonStr = """
            {
                "title": "Baby Shark Dance",
                "author_name": "Pinkfong Baby Shark",
                "author_url": "https://www.youtube.com/channel/UC-yBVzHNBKEx34GBJf8WNQA",
                "type": "video",
                "thumbnail_url": "https://i.ytimg.com/vi/XqZsoesa55w/hqdefault.jpg",
                "thumbnail_width": 480,
                "thumbnail_height": 360,
                "width": 200,
                "height": 113,
                "html": "<iframe></iframe>",
                "version": "1.0",
                "provider_name": "YouTube",
                "provider_url": "https://www.youtube.com/"
            }
        """.trimIndent()

        val response = json.decodeFromString<OEmbedResponse>(jsonStr)

        assertThat(response.title).isEqualTo("Baby Shark Dance")
        assertThat(response.authorName).isEqualTo("Pinkfong Baby Shark")
        assertThat(response.authorUrl).isEqualTo("https://www.youtube.com/channel/UC-yBVzHNBKEx34GBJf8WNQA")
        assertThat(response.thumbnailUrl).isEqualTo("https://i.ytimg.com/vi/XqZsoesa55w/hqdefault.jpg")
        assertThat(response.type).isEqualTo("video")
    }

    @Test
    fun `deserialize playlist oEmbed response`() {
        val jsonStr = """
            {
                "title": "Peppa Pig Full Episodes",
                "author_name": "Peppa Pig Official Channel",
                "author_url": "https://www.youtube.com/channel/UCAOtE1V7Ots4DjM8JLlrYgg",
                "type": "rich",
                "thumbnail_url": "https://i.ytimg.com/vi/abc123/hqdefault.jpg"
            }
        """.trimIndent()

        val response = json.decodeFromString<OEmbedResponse>(jsonStr)

        assertThat(response.title).isEqualTo("Peppa Pig Full Episodes")
        assertThat(response.authorName).isEqualTo("Peppa Pig Official Channel")
        assertThat(response.type).isEqualTo("rich")
    }

    @Test
    fun `extract channel ID from author_url`() {
        val authorUrl = "https://www.youtube.com/channel/UC-yBVzHNBKEx34GBJf8WNQA"
        val channelId = authorUrl.substringAfterLast("/channel/")

        assertThat(channelId).isEqualTo("UC-yBVzHNBKEx34GBJf8WNQA")
    }

    @Test
    fun `unknown fields are ignored during deserialization`() {
        val jsonStr = """
            {
                "title": "Test",
                "author_name": "Author",
                "author_url": "https://www.youtube.com/channel/UC123",
                "thumbnail_url": "https://i.ytimg.com/vi/test/hqdefault.jpg",
                "type": "video",
                "unknown_field": "should be ignored",
                "another_field": 42
            }
        """.trimIndent()

        val response = json.decodeFromString<OEmbedResponse>(jsonStr)

        assertThat(response.title).isEqualTo("Test")
    }
}
