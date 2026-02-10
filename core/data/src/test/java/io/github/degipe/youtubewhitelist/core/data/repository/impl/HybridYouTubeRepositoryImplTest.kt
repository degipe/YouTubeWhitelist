package io.github.degipe.youtubewhitelist.core.data.repository.impl

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.network.api.YouTubeApiService
import io.github.degipe.youtubewhitelist.core.network.dto.ChannelContentDetails
import io.github.degipe.youtubewhitelist.core.network.dto.ChannelDto
import io.github.degipe.youtubewhitelist.core.network.dto.ChannelSnippet
import io.github.degipe.youtubewhitelist.core.network.dto.ChannelStatistics
import io.github.degipe.youtubewhitelist.core.network.dto.PlaylistDto
import io.github.degipe.youtubewhitelist.core.network.dto.PlaylistSnippet
import io.github.degipe.youtubewhitelist.core.network.dto.RelatedPlaylists
import io.github.degipe.youtubewhitelist.core.network.dto.Thumbnail
import io.github.degipe.youtubewhitelist.core.network.dto.ThumbnailSet
import io.github.degipe.youtubewhitelist.core.network.dto.VideoContentDetails
import io.github.degipe.youtubewhitelist.core.network.dto.VideoDto
import io.github.degipe.youtubewhitelist.core.network.dto.VideoSnippet
import io.github.degipe.youtubewhitelist.core.network.dto.YouTubeListResponse
import io.github.degipe.youtubewhitelist.core.network.invidious.InvidiousApiService
import io.github.degipe.youtubewhitelist.core.network.invidious.InvidiousChannelDto
import io.github.degipe.youtubewhitelist.core.network.invidious.InvidiousInstanceManager
import io.github.degipe.youtubewhitelist.core.network.invidious.InvidiousPlaylistDto
import io.github.degipe.youtubewhitelist.core.network.invidious.InvidiousPlaylistVideoDto
import io.github.degipe.youtubewhitelist.core.network.invidious.InvidiousVideoDto
import io.github.degipe.youtubewhitelist.core.network.oembed.OEmbedResponse
import io.github.degipe.youtubewhitelist.core.network.oembed.OEmbedService
import io.github.degipe.youtubewhitelist.core.network.rss.RssFeedParser
import io.github.degipe.youtubewhitelist.core.network.rss.RssVideoEntry
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class HybridYouTubeRepositoryImplTest {

    private lateinit var apiService: YouTubeApiService
    private lateinit var oEmbedService: OEmbedService
    private lateinit var rssFeedParser: RssFeedParser
    private lateinit var invidiousApiService: InvidiousApiService
    private lateinit var invidiousInstanceManager: InvidiousInstanceManager
    private lateinit var repository: HybridYouTubeRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    private val testThumbnails = ThumbnailSet(
        default = null,
        medium = null,
        high = Thumbnail("https://i.ytimg.com/vi/test/hqdefault.jpg")
    )

    @Before
    fun setUp() {
        apiService = mockk()
        oEmbedService = mockk()
        rssFeedParser = mockk()
        invidiousApiService = mockk()
        invidiousInstanceManager = mockk()
        repository = HybridYouTubeRepositoryImpl(
            youTubeApiService = apiService,
            oEmbedService = oEmbedService,
            rssFeedParser = rssFeedParser,
            invidiousApiService = invidiousApiService,
            invidiousInstanceManager = invidiousInstanceManager,
            ioDispatcher = testDispatcher
        )
    }

    // === getVideoById ===

    @Test
    fun `getVideoById - oEmbed success returns video metadata`() = runTest(testDispatcher) {
        coEvery { oEmbedService.getOEmbed(any(), any()) } returns Response.success(
            OEmbedResponse(
                title = "Baby Shark",
                authorName = "Pinkfong",
                authorUrl = "https://www.youtube.com/channel/UC123",
                thumbnailUrl = "https://i.ytimg.com/vi/abc/hqdefault.jpg",
                type = "video"
            )
        )

        val result = repository.getVideoById("abc")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val video = (result as AppResult.Success).data
        assertThat(video.youtubeId).isEqualTo("abc")
        assertThat(video.title).isEqualTo("Baby Shark")
        assertThat(video.channelTitle).isEqualTo("Pinkfong")
        assertThat(video.channelId).isEqualTo("UC123")
    }

    @Test
    fun `getVideoById - oEmbed fails, API success`() = runTest(testDispatcher) {
        coEvery { oEmbedService.getOEmbed(any(), any()) } throws IOException("oEmbed down")
        coEvery { apiService.getVideos(any(), eq("abc")) } returns Response.success(
            YouTubeListResponse(
                items = listOf(
                    VideoDto(
                        id = "abc",
                        snippet = VideoSnippet(
                            title = "Baby Shark",
                            channelId = "UC123",
                            channelTitle = "Pinkfong",
                            description = "Dance",
                            thumbnails = testThumbnails
                        ),
                        contentDetails = VideoContentDetails("PT3M")
                    )
                )
            )
        )

        val result = repository.getVideoById("abc")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val video = (result as AppResult.Success).data
        assertThat(video.title).isEqualTo("Baby Shark")
        assertThat(video.duration).isEqualTo("PT3M")
    }

    @Test
    fun `getVideoById - oEmbed fails, API fails, Invidious success`() = runTest(testDispatcher) {
        coEvery { oEmbedService.getOEmbed(any(), any()) } throws IOException("oEmbed down")
        coEvery { apiService.getVideos(any(), any()) } throws IOException("API down")
        every { invidiousInstanceManager.getHealthyInstance() } returns "https://yewtu.be"
        every { invidiousInstanceManager.reportSuccess(any()) } returns Unit
        coEvery { invidiousApiService.getVideo("https://yewtu.be", "abc") } returns InvidiousVideoDto(
            videoId = "abc",
            title = "Baby Shark",
            author = "Pinkfong",
            authorId = "UC123"
        )

        val result = repository.getVideoById("abc")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val video = (result as AppResult.Success).data
        assertThat(video.title).isEqualTo("Baby Shark")
    }

    @Test
    fun `getVideoById - all sources fail returns error`() = runTest(testDispatcher) {
        coEvery { oEmbedService.getOEmbed(any(), any()) } throws IOException("oEmbed down")
        coEvery { apiService.getVideos(any(), any()) } throws IOException("API down")
        every { invidiousInstanceManager.getHealthyInstance() } returns null

        val result = repository.getVideoById("abc")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
    }

    // === getPlaylistById ===

    @Test
    fun `getPlaylistById - oEmbed success returns playlist metadata`() = runTest(testDispatcher) {
        coEvery { oEmbedService.getOEmbed(any(), any()) } returns Response.success(
            OEmbedResponse(
                title = "Peppa Pig Episodes",
                authorName = "Peppa Pig",
                authorUrl = "https://www.youtube.com/channel/UCpeppa",
                thumbnailUrl = "https://i.ytimg.com/vi/xyz/hqdefault.jpg",
                type = "rich"
            )
        )

        val result = repository.getPlaylistById("PLtest")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val playlist = (result as AppResult.Success).data
        assertThat(playlist.youtubeId).isEqualTo("PLtest")
        assertThat(playlist.title).isEqualTo("Peppa Pig Episodes")
    }

    @Test
    fun `getPlaylistById - fallback chain reaches Invidious`() = runTest(testDispatcher) {
        coEvery { oEmbedService.getOEmbed(any(), any()) } throws IOException("down")
        coEvery { apiService.getPlaylists(any(), any()) } throws IOException("API down")
        every { invidiousInstanceManager.getHealthyInstance() } returns "https://yewtu.be"
        every { invidiousInstanceManager.reportSuccess(any()) } returns Unit
        coEvery { invidiousApiService.getPlaylist("https://yewtu.be", "PLtest") } returns InvidiousPlaylistDto(
            playlistId = "PLtest",
            title = "Peppa Pig Episodes",
            author = "Peppa Pig",
            authorId = "UCpeppa"
        )

        val result = repository.getPlaylistById("PLtest")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        assertThat((result as AppResult.Success).data.title).isEqualTo("Peppa Pig Episodes")
    }

    // === getChannelById ===

    @Test
    fun `getChannelById - API success (no oEmbed for channels)`() = runTest(testDispatcher) {
        coEvery { apiService.getChannels(any(), eq("UC123"), any()) } returns Response.success(
            YouTubeListResponse(
                items = listOf(
                    ChannelDto(
                        id = "UC123",
                        snippet = ChannelSnippet(
                            title = "Test Channel",
                            description = "Desc",
                            thumbnails = testThumbnails
                        ),
                        statistics = ChannelStatistics(subscriberCount = "1000", videoCount = "50"),
                        contentDetails = ChannelContentDetails(RelatedPlaylists(uploads = "UU123"))
                    )
                )
            )
        )

        val result = repository.getChannelById("UC123")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val channel = (result as AppResult.Success).data
        assertThat(channel.title).isEqualTo("Test Channel")
        assertThat(channel.uploadsPlaylistId).isEqualTo("UU123")
    }

    @Test
    fun `getChannelById - API fails, Invidious success`() = runTest(testDispatcher) {
        coEvery { apiService.getChannels(any(), any(), any()) } throws IOException("API down")
        every { invidiousInstanceManager.getHealthyInstance() } returns "https://yewtu.be"
        every { invidiousInstanceManager.reportSuccess(any()) } returns Unit
        coEvery { invidiousApiService.getChannel("https://yewtu.be", "UC123") } returns InvidiousChannelDto(
            authorId = "UC123",
            author = "Test Channel"
        )

        val result = repository.getChannelById("UC123")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        assertThat((result as AppResult.Success).data.title).isEqualTo("Test Channel")
    }

    // === getChannelByHandle ===

    @Test
    fun `getChannelByHandle - API success`() = runTest(testDispatcher) {
        coEvery { apiService.getChannels(any(), any(), eq("testhandle")) } returns Response.success(
            YouTubeListResponse(
                items = listOf(
                    ChannelDto(
                        id = "UC123",
                        snippet = ChannelSnippet(
                            title = "Test Channel",
                            description = "Desc",
                            thumbnails = testThumbnails
                        ),
                        statistics = null,
                        contentDetails = null
                    )
                )
            )
        )

        val result = repository.getChannelByHandle("testhandle")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        assertThat((result as AppResult.Success).data.title).isEqualTo("Test Channel")
    }

    @Test
    fun `getChannelByHandle - API fails, Invidious resolves`() = runTest(testDispatcher) {
        coEvery { apiService.getChannels(any(), any(), any()) } throws IOException("API down")
        every { invidiousInstanceManager.getHealthyInstance() } returns "https://yewtu.be"
        every { invidiousInstanceManager.reportSuccess(any()) } returns Unit
        coEvery { invidiousApiService.resolveChannel("https://yewtu.be", "testhandle") } returns "UC123"
        coEvery { invidiousApiService.getChannel("https://yewtu.be", "UC123") } returns InvidiousChannelDto(
            authorId = "UC123",
            author = "Test Channel"
        )

        val result = repository.getChannelByHandle("testhandle")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        assertThat((result as AppResult.Success).data.youtubeId).isEqualTo("UC123")
    }

    // === getPlaylistItems ===

    @Test
    fun `getPlaylistItems - RSS success for uploads playlist`() = runTest(testDispatcher) {
        coEvery { rssFeedParser.fetchChannelVideos("UC123") } returns listOf(
            RssVideoEntry("v1", "Video 1", "https://i.ytimg.com/vi/v1/hqdefault.jpg", "Channel", "2026-01-01"),
            RssVideoEntry("v2", "Video 2", "https://i.ytimg.com/vi/v2/hqdefault.jpg", "Channel", "2026-01-02")
        )

        // UU123 = uploads playlist for UC123
        val result = repository.getPlaylistItems("UU123")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val videos = (result as AppResult.Success).data
        assertThat(videos).hasSize(2)
        assertThat(videos[0].videoId).isEqualTo("v1")
        assertThat(videos[0].title).isEqualTo("Video 1")
    }

    @Test
    fun `getPlaylistItems - RSS fails, API success`() = runTest(testDispatcher) {
        coEvery { rssFeedParser.fetchChannelVideos(any()) } returns emptyList()
        coEvery { apiService.getPlaylistItems(any(), eq("UU123"), any(), any()) } returns Response.success(
            YouTubeListResponse(items = emptyList())
        )

        val result = repository.getPlaylistItems("UU123")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
    }

    @Test
    fun `getPlaylistItems - non-uploads playlist skips RSS`() = runTest(testDispatcher) {
        // PLabc is not an uploads playlist, so RSS is skipped
        coEvery { apiService.getPlaylistItems(any(), eq("PLabc"), any(), any()) } returns Response.success(
            YouTubeListResponse(items = emptyList())
        )

        val result = repository.getPlaylistItems("PLabc")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
    }
}
