package io.github.degipe.youtubewhitelist.core.network.rss

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import org.junit.Before
import org.junit.Test

class RssFeedParserTest {

    private lateinit var parser: RssFeedParser

    @Before
    fun setUp() {
        // OkHttpClient is only used for fetchChannelVideos (network), not parseXml
        parser = RssFeedParser(mockk())
    }

    @Test
    fun `fetchChannelVideos returns empty list and closes response body on server error`() = runTest {
        // Regression test for KM1: an unsuccessful response's body must always be
        // consumed/closed, or OkHttp never releases the connection back to its pool.
        val trackingBody = TrackingResponseBody("Internal Server Error")
        val response = Response.Builder()
            .request(Request.Builder().url("https://www.youtube.com/feeds/videos.xml?channel_id=UC123").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body(trackingBody)
            .build()

        val call = mockk<Call>()
        every { call.execute() } returns response
        val client = mockk<OkHttpClient>()
        every { client.newCall(any()) } returns call

        val errorParser = RssFeedParser(client)

        val result = errorParser.fetchChannelVideos("UC123")

        assertThat(result).isEmpty()
        assertThat(trackingBody.closed).isTrue()
    }

    /** A [ResponseBody] that records whether [close] was called, to assert no connection leak. */
    private class TrackingResponseBody(content: String) : ResponseBody() {
        private val buffer = Buffer().writeUtf8(content)

        var closed = false
            private set

        override fun contentType(): MediaType? = "text/plain".toMediaTypeOrNull()
        override fun contentLength(): Long = buffer.size
        override fun source(): BufferedSource = buffer
        override fun close() {
            closed = true
            super.close()
        }
    }

    @Test
    fun `parse valid RSS XML returns video entries`() {
        val xml = VALID_RSS_XML

        val entries = parser.parseXml(xml)

        assertThat(entries).hasSize(2)
        assertThat(entries[0].videoId).isEqualTo("abc123")
        assertThat(entries[0].title).isEqualTo("First Video")
        assertThat(entries[0].channelTitle).isEqualTo("Test Channel")
        assertThat(entries[0].published).isEqualTo("2026-01-15T10:00:00+00:00")
        assertThat(entries[0].thumbnailUrl).isEqualTo("https://i.ytimg.com/vi/abc123/hqdefault.jpg")

        assertThat(entries[1].videoId).isEqualTo("def456")
        assertThat(entries[1].title).isEqualTo("Second Video")
    }

    @Test
    fun `empty feed returns empty list`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom"
                  xmlns:yt="urn:youtube"
                  xmlns:media="http://search.yahoo.com/mrss/">
                <title>Empty Channel</title>
            </feed>
        """.trimIndent()

        val entries = parser.parseXml(xml)

        assertThat(entries).isEmpty()
    }

    @Test
    fun `malformed XML returns empty list`() {
        val xml = "this is not xml at all <broken>"

        val entries = parser.parseXml(xml)

        assertThat(entries).isEmpty()
    }

    @Test
    fun `entry with missing videoId is skipped`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom"
                  xmlns:yt="urn:youtube"
                  xmlns:media="http://search.yahoo.com/mrss/">
                <title>Test Channel</title>
                <entry>
                    <title>No Video ID</title>
                    <published>2026-01-15T10:00:00+00:00</published>
                </entry>
                <entry>
                    <yt:videoId>valid123</yt:videoId>
                    <title>Valid Entry</title>
                    <published>2026-01-14T10:00:00+00:00</published>
                </entry>
            </feed>
        """.trimIndent()

        val entries = parser.parseXml(xml)

        assertThat(entries).hasSize(1)
        assertThat(entries[0].videoId).isEqualTo("valid123")
    }

    @Test
    fun `thumbnail URL is built from videoId`() {
        val xml = VALID_RSS_XML

        val entries = parser.parseXml(xml)

        assertThat(entries[0].thumbnailUrl).isEqualTo("https://i.ytimg.com/vi/abc123/hqdefault.jpg")
        assertThat(entries[1].thumbnailUrl).isEqualTo("https://i.ytimg.com/vi/def456/hqdefault.jpg")
    }

    companion object {
        private val VALID_RSS_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom"
                  xmlns:yt="urn:youtube"
                  xmlns:media="http://search.yahoo.com/mrss/">
                <title>Test Channel</title>
                <entry>
                    <yt:videoId>abc123</yt:videoId>
                    <title>First Video</title>
                    <published>2026-01-15T10:00:00+00:00</published>
                    <media:group>
                        <media:thumbnail url="https://i.ytimg.com/vi/abc123/hqdefault.jpg"/>
                    </media:group>
                </entry>
                <entry>
                    <yt:videoId>def456</yt:videoId>
                    <title>Second Video</title>
                    <published>2026-01-14T10:00:00+00:00</published>
                    <media:group>
                        <media:thumbnail url="https://i.ytimg.com/vi/def456/hqdefault.jpg"/>
                    </media:group>
                </entry>
            </feed>
        """.trimIndent()
    }
}
