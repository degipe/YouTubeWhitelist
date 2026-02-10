package io.github.degipe.youtubewhitelist.core.network.rss

import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
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
