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
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class YouTubeApiRepositoryImplTest {

    private lateinit var apiService: YouTubeApiService
    private lateinit var repository: YouTubeApiRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        apiService = mockk()
        repository = YouTubeApiRepositoryImpl(apiService, testDispatcher)
    }

    // === getChannelById ===

    @Test
    fun `getChannelById returns success with mapped channel`() = runTest(testDispatcher) {
        val channelDto = createChannelDto(
            id = "UC123",
            title = "Test Channel",
            description = "A test channel",
            subscriberCount = "50000",
            videoCount = "100",
            uploadsPlaylistId = "UU123",
            thumbnailUrl = "https://img/high.jpg"
        )
        coEvery { apiService.getChannels(id = "UC123") } returns
            Response.success(YouTubeListResponse(items = listOf(channelDto)))

        val result = repository.getChannelById("UC123")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val channel = (result as AppResult.Success).data
        assertThat(channel.youtubeId).isEqualTo("UC123")
        assertThat(channel.title).isEqualTo("Test Channel")
        assertThat(channel.description).isEqualTo("A test channel")
        assertThat(channel.subscriberCount).isEqualTo("50000")
        assertThat(channel.videoCount).isEqualTo("100")
        assertThat(channel.uploadsPlaylistId).isEqualTo("UU123")
        assertThat(channel.thumbnailUrl).isEqualTo("https://img/high.jpg")
    }

    @Test
    fun `getChannelById returns error when channel not found`() = runTest(testDispatcher) {
        coEvery { apiService.getChannels(id = "UC_nonexistent") } returns
            Response.success(YouTubeListResponse(items = emptyList()))

        val result = repository.getChannelById("UC_nonexistent")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).message).contains("not found")
    }

    @Test
    fun `getChannelById returns error for network failure`() = runTest(testDispatcher) {
        coEvery { apiService.getChannels(id = "UC123") } throws IOException("No internet")

        val result = repository.getChannelById("UC123")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).exception).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `getChannelById returns error for http error response`() = runTest(testDispatcher) {
        coEvery { apiService.getChannels(id = "UC123") } returns
            Response.error(403, "Quota exceeded".toResponseBody())

        val result = repository.getChannelById("UC123")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).message).contains("API error")
    }

    // === getChannelByHandle ===

    @Test
    fun `getChannelByHandle resolves handle to channel`() = runTest(testDispatcher) {
        val channelDto = createChannelDto(id = "UC456", title = "Handle Channel")
        coEvery { apiService.getChannels(forHandle = "MrBeast") } returns
            Response.success(YouTubeListResponse(items = listOf(channelDto)))

        val result = repository.getChannelByHandle("MrBeast")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val channel = (result as AppResult.Success).data
        assertThat(channel.youtubeId).isEqualTo("UC456")
        assertThat(channel.title).isEqualTo("Handle Channel")
    }

    // === getVideoById ===

    @Test
    fun `getVideoById returns success with mapped video`() = runTest(testDispatcher) {
        val videoDto = VideoDto(
            id = "vid123",
            snippet = VideoSnippet(
                title = "Test Video",
                description = "A test video",
                channelId = "UC123",
                channelTitle = "Test Channel",
                thumbnails = ThumbnailSet(
                    high = Thumbnail(url = "https://img/high.jpg")
                )
            ),
            contentDetails = VideoContentDetails(duration = "PT4M33S")
        )
        coEvery { apiService.getVideos(id = "vid123") } returns
            Response.success(YouTubeListResponse(items = listOf(videoDto)))

        val result = repository.getVideoById("vid123")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val video = (result as AppResult.Success).data
        assertThat(video.youtubeId).isEqualTo("vid123")
        assertThat(video.title).isEqualTo("Test Video")
        assertThat(video.channelId).isEqualTo("UC123")
        assertThat(video.channelTitle).isEqualTo("Test Channel")
        assertThat(video.duration).isEqualTo("PT4M33S")
        assertThat(video.thumbnailUrl).isEqualTo("https://img/high.jpg")
    }

    @Test
    fun `getVideoById returns error when video not found`() = runTest(testDispatcher) {
        coEvery { apiService.getVideos(id = "nonexistent") } returns
            Response.success(YouTubeListResponse(items = emptyList()))

        val result = repository.getVideoById("nonexistent")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
    }

    // === getPlaylistById ===

    @Test
    fun `getPlaylistById returns success with mapped playlist`() = runTest(testDispatcher) {
        val playlistDto = PlaylistDto(
            id = "PL123",
            snippet = PlaylistSnippet(
                title = "Test Playlist",
                description = "A test playlist",
                channelId = "UC123",
                channelTitle = "Test Channel",
                thumbnails = ThumbnailSet(
                    medium = Thumbnail(url = "https://img/medium.jpg")
                )
            )
        )
        coEvery { apiService.getPlaylists(id = "PL123") } returns
            Response.success(YouTubeListResponse(items = listOf(playlistDto)))

        val result = repository.getPlaylistById("PL123")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val playlist = (result as AppResult.Success).data
        assertThat(playlist.youtubeId).isEqualTo("PL123")
        assertThat(playlist.title).isEqualTo("Test Playlist")
        assertThat(playlist.channelTitle).isEqualTo("Test Channel")
        assertThat(playlist.thumbnailUrl).isEqualTo("https://img/medium.jpg")
    }

    // === Thumbnail selection ===

    @Test
    fun `thumbnail selection prefers high quality`() = runTest(testDispatcher) {
        val channelDto = createChannelDto(
            id = "UC123",
            title = "Channel",
            thumbnails = ThumbnailSet(
                default = Thumbnail(url = "https://img/default.jpg"),
                medium = Thumbnail(url = "https://img/medium.jpg"),
                high = Thumbnail(url = "https://img/high.jpg")
            )
        )
        coEvery { apiService.getChannels(id = "UC123") } returns
            Response.success(YouTubeListResponse(items = listOf(channelDto)))

        val result = repository.getChannelById("UC123")
        assertThat((result as AppResult.Success).data.thumbnailUrl).isEqualTo("https://img/high.jpg")
    }

    @Test
    fun `thumbnail falls back to medium then default`() = runTest(testDispatcher) {
        val channelDto = createChannelDto(
            id = "UC123",
            title = "Channel",
            thumbnails = ThumbnailSet(
                default = Thumbnail(url = "https://img/default.jpg"),
                medium = Thumbnail(url = "https://img/medium.jpg")
            )
        )
        coEvery { apiService.getChannels(id = "UC123") } returns
            Response.success(YouTubeListResponse(items = listOf(channelDto)))

        val result = repository.getChannelById("UC123")
        assertThat((result as AppResult.Success).data.thumbnailUrl).isEqualTo("https://img/medium.jpg")
    }

    @Test
    fun `thumbnail falls back to default when others missing`() = runTest(testDispatcher) {
        val channelDto = createChannelDto(
            id = "UC123",
            title = "Channel",
            thumbnails = ThumbnailSet(
                default = Thumbnail(url = "https://img/default.jpg")
            )
        )
        coEvery { apiService.getChannels(id = "UC123") } returns
            Response.success(YouTubeListResponse(items = listOf(channelDto)))

        val result = repository.getChannelById("UC123")
        assertThat((result as AppResult.Success).data.thumbnailUrl).isEqualTo("https://img/default.jpg")
    }

    // === Helper ===

    private fun createChannelDto(
        id: String = "UC123",
        title: String = "Test Channel",
        description: String = "",
        subscriberCount: String? = null,
        videoCount: String? = null,
        uploadsPlaylistId: String? = null,
        thumbnailUrl: String = "https://img/default.jpg",
        thumbnails: ThumbnailSet? = null
    ): ChannelDto = ChannelDto(
        id = id,
        snippet = ChannelSnippet(
            title = title,
            description = description,
            thumbnails = thumbnails ?: ThumbnailSet(
                high = Thumbnail(url = thumbnailUrl)
            )
        ),
        contentDetails = ChannelContentDetails(
            relatedPlaylists = RelatedPlaylists(uploads = uploadsPlaylistId)
        ),
        statistics = ChannelStatistics(
            subscriberCount = subscriberCount,
            videoCount = videoCount
        )
    )
}
