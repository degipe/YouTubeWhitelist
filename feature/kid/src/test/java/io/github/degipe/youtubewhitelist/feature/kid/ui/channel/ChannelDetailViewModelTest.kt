package io.github.degipe.youtubewhitelist.feature.kid.ui.channel

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.model.PlaylistVideo
import io.github.degipe.youtubewhitelist.core.data.model.YouTubeMetadata
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChannelDetailViewModelTest {

    private lateinit var youTubeApiRepository: YouTubeApiRepository
    private val testDispatcher = StandardTestDispatcher()

    private val testChannel = YouTubeMetadata.Channel(
        youtubeId = "UC123",
        title = "Fun Channel",
        thumbnailUrl = "https://img/channel.jpg",
        description = "A fun channel",
        subscriberCount = "1000",
        videoCount = "50",
        uploadsPlaylistId = "UU123"
    )

    private fun makeVideo(id: String, title: String, position: Int) = PlaylistVideo(
        videoId = id,
        title = title,
        thumbnailUrl = "https://img/$id.jpg",
        channelTitle = "Fun Channel",
        position = position
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        youTubeApiRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        channelId: String = "UC123",
        channelTitle: String = "Fun Channel"
    ): ChannelDetailViewModel {
        return ChannelDetailViewModel(
            youTubeApiRepository = youTubeApiRepository,
            channelId = channelId,
            channelTitle = channelTitle
        )
    }

    @Test
    fun `initial state is loading with channel title`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItems("UU123") } returns AppResult.Success(emptyList())

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.isLoading).isTrue()
        assertThat(viewModel.uiState.value.channelTitle).isEqualTo("Fun Channel")
    }

    @Test
    fun `loads videos from channel uploads playlist`() = runTest(testDispatcher) {
        val videos = listOf(makeVideo("v1", "Video 1", 0), makeVideo("v2", "Video 2", 1))
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItems("UU123") } returns AppResult.Success(videos)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).hasSize(2)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `videos sorted by position`() = runTest(testDispatcher) {
        val videos = listOf(makeVideo("v2", "Video 2", 2), makeVideo("v1", "Video 1", 0), makeVideo("v3", "Video 3", 1))
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItems("UU123") } returns AppResult.Success(videos)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos.map { it.videoId }).containsExactly("v1", "v3", "v2").inOrder()
    }

    @Test
    fun `empty list when channel has no videos`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItems("UU123") } returns AppResult.Success(emptyList())

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).isEmpty()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `error state when channel fetch fails`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Error("API error: 404")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isEqualTo("API error: 404")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `error state when playlist fetch fails`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItems("UU123") } returns AppResult.Error("Network error")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isEqualTo("Network error")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `error when uploads playlist is null`() = runTest(testDispatcher) {
        val channelNoUploads = testChannel.copy(uploadsPlaylistId = null)
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(channelNoUploads)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).contains("uploads playlist not found")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `retry reloads videos`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Error("Network error")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isNotNull()

        // Fix the error and retry
        val videos = listOf(makeVideo("v1", "Video 1", 0))
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItems("UU123") } returns AppResult.Success(videos)

        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).hasSize(1)
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `loading state set on retry`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Error("fail")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItems("UU123") } returns AppResult.Success(emptyList())

        viewModel.retry()

        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }
}
