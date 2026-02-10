package io.github.degipe.youtubewhitelist.core.network.rss

import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

class RssFeedParser(private val okHttpClient: OkHttpClient) {

    suspend fun fetchChannelVideos(channelId: String): List<RssVideoEntry> {
        val url = "https://www.youtube.com/feeds/videos.xml?channel_id=$channelId"
        return try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            parseXml(body)
        } catch (_: Exception) {
            emptyList()
        }
    }

    internal fun parseXml(xml: String): List<RssVideoEntry> {
        return try {
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                // XXE protection
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                isXIncludeAware = false
                isExpandEntityReferences = false
            }
            val builder = factory.newDocumentBuilder()
            val document = builder.parse(ByteArrayInputStream(xml.toByteArray()))

            val entries = document.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "entry")
            val channelTitle = extractChannelTitle(document)

            val result = mutableListOf<RssVideoEntry>()
            for (i in 0 until entries.length) {
                val entry = entries.item(i) as? Element ?: continue
                val videoEntry = parseEntry(entry, channelTitle) ?: continue
                result.add(videoEntry)
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun extractChannelTitle(document: org.w3c.dom.Document): String {
        val titles = document.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "title")
        return if (titles.length > 0) titles.item(0).textContent ?: "" else ""
    }

    private fun parseEntry(entry: Element, channelTitle: String): RssVideoEntry? {
        val videoId = getElementTextNS(entry, "urn:youtube", "videoId") ?: return null
        val title = getElementTextNS(entry, "http://www.w3.org/2005/Atom", "title") ?: ""
        val published = getElementTextNS(entry, "http://www.w3.org/2005/Atom", "published") ?: ""
        val thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

        return RssVideoEntry(
            videoId = videoId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            channelTitle = channelTitle,
            published = published
        )
    }

    private fun getElementTextNS(parent: Element, namespaceUri: String, localName: String): String? {
        val nodes: NodeList = parent.getElementsByTagNameNS(namespaceUri, localName)
        return if (nodes.length > 0) nodes.item(0).textContent else null
    }
}
