package io.github.degipe.youtubewhitelist.core.common.youtube

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class YouTubeUrlParserTest {

    // === VIDEO URLs ===

    @Test
    fun `parse standard video url`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("dQw4w9WgXcQ")
    }

    @Test
    fun `parse video url without www`() {
        val result = YouTubeUrlParser.parse("https://youtube.com/watch?v=dQw4w9WgXcQ")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("dQw4w9WgXcQ")
    }

    @Test
    fun `parse video url with http`() {
        val result = YouTubeUrlParser.parse("http://www.youtube.com/watch?v=dQw4w9WgXcQ")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("dQw4w9WgXcQ")
    }

    @Test
    fun `parse short video url`() {
        val result = YouTubeUrlParser.parse("https://youtu.be/dQw4w9WgXcQ")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("dQw4w9WgXcQ")
    }

    @Test
    fun `parse shorts url`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/shorts/dQw4w9WgXcQ")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("dQw4w9WgXcQ")
    }

    @Test
    fun `parse embed url`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/embed/dQw4w9WgXcQ")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("dQw4w9WgXcQ")
    }

    @Test
    fun `parse live url`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/live/dQw4w9WgXcQ")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("dQw4w9WgXcQ")
    }

    @Test
    fun `parse mobile video url`() {
        val result = YouTubeUrlParser.parse("https://m.youtube.com/watch?v=dQw4w9WgXcQ")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("dQw4w9WgXcQ")
    }

    @Test
    fun `parse video url with extra params`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=30s&feature=share")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("dQw4w9WgXcQ")
    }

    @Test
    fun `parse video url with fragment`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ#anchor")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("dQw4w9WgXcQ")
    }

    @Test
    fun `parse video id with hyphen and underscore`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/watch?v=a-B_c1D2e3F")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("a-B_c1D2e3F")
    }

    @Test
    fun `parse short url with query params`() {
        val result = YouTubeUrlParser.parse("https://youtu.be/dQw4w9WgXcQ?t=30")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("dQw4w9WgXcQ")
    }

    // === CHANNEL URLs ===

    @Test
    fun `parse channel url with id`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/channel/UCxxxxxxxxxxxxxxxxxxxxxx")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.CHANNEL)
        assertThat(result.id).isEqualTo("UCxxxxxxxxxxxxxxxxxxxxxx")
    }

    @Test
    fun `parse channel handle url`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/@MrBeast")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.CHANNEL_HANDLE)
        assertThat(result.id).isEqualTo("MrBeast")
    }

    @Test
    fun `parse channel custom url`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/c/PewDiePie")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.CHANNEL_CUSTOM)
        assertThat(result.id).isEqualTo("PewDiePie")
    }

    @Test
    fun `parse channel url with trailing slash`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/channel/UCxxxxxxxxxxxxxxxxxxxxxx/")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.CHANNEL)
        assertThat(result.id).isEqualTo("UCxxxxxxxxxxxxxxxxxxxxxx")
    }

    @Test
    fun `parse channel handle with trailing slash`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/@MrBeast/")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.CHANNEL_HANDLE)
        assertThat(result.id).isEqualTo("MrBeast")
    }

    @Test
    fun `parse channel handle with subpath`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/@MrBeast/videos")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.CHANNEL_HANDLE)
        assertThat(result.id).isEqualTo("MrBeast")
    }

    @Test
    fun `parse channel id with subpath`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/channel/UCxxxxxxxxxxxxxxxxxxxxxx/featured")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.CHANNEL)
        assertThat(result.id).isEqualTo("UCxxxxxxxxxxxxxxxxxxxxxx")
    }

    // === PLAYLIST URLs ===

    @Test
    fun `parse playlist url`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/playlist?list=PLxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.PLAYLIST)
        assertThat(result.id).isEqualTo("PLxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
    }

    @Test
    fun `parse video url with playlist prefers playlist`() {
        val result = YouTubeUrlParser.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=PLxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.PLAYLIST)
        assertThat(result.id).isEqualTo("PLxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
    }

    // === EDGE CASES ===

    @Test
    fun `parse returns null for non-youtube url`() {
        assertThat(YouTubeUrlParser.parse("https://www.google.com")).isNull()
    }

    @Test
    fun `parse returns null for empty string`() {
        assertThat(YouTubeUrlParser.parse("")).isNull()
    }

    @Test
    fun `parse returns null for blank string`() {
        assertThat(YouTubeUrlParser.parse("   ")).isNull()
    }

    @Test
    fun `parse returns null for malformed url`() {
        assertThat(YouTubeUrlParser.parse("not a url")).isNull()
    }

    @Test
    fun `parse returns null for youtube homepage`() {
        assertThat(YouTubeUrlParser.parse("https://www.youtube.com")).isNull()
    }

    @Test
    fun `parse returns null for youtube homepage with slash`() {
        assertThat(YouTubeUrlParser.parse("https://www.youtube.com/")).isNull()
    }

    @Test
    fun `parse handles uppercase host`() {
        val result = YouTubeUrlParser.parse("https://WWW.YOUTUBE.COM/watch?v=dQw4w9WgXcQ")
        assertThat(result).isNotNull()
        assertThat(result!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(result.id).isEqualTo("dQw4w9WgXcQ")
    }

    @Test
    fun `parse returns null for watch url without v param`() {
        assertThat(YouTubeUrlParser.parse("https://www.youtube.com/watch")).isNull()
    }

    @Test
    fun `parse returns null for youtube feed url`() {
        assertThat(YouTubeUrlParser.parse("https://www.youtube.com/feed/trending")).isNull()
    }
}
