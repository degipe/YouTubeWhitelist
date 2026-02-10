package io.github.degipe.youtubewhitelist.feature.kid.ui.channel

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.model.PaginatedPlaylistResult
import io.github.degipe.youtubewhitelist.core.data.model.PlaylistVideo
import io.github.degipe.youtubewhitelist.core.data.model.YouTubeMetadata
import io.github.degipe.youtubewhitelist.core.data.repository.ChannelVideoCacheRepository
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChannelDetailViewModelTest {

    private lateinit var youTubeApiRepository: YouTubeApiRepository
    private lateinit var channelVideoCacheRepository: ChannelVideoCacheRepository
    private val testDispatcher = StandardTestDispatcher()

    private val cachedVideosFlow = MutableStateFlow<List<PlaylistVideo>>(emptyList())
    private val searchResultsFlow = MutableStateFlow<List<PlaylistVideo>>(emptyList())

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
        channelVideoCacheRepository = mockk()

        every { channelVideoCacheRepository.getVideos("UC123") } returns cachedVideosFlow
        every { channelVideoCacheRepository.searchVideos("UC123", any()) } returns searchResultsFlow
        coEvery { channelVideoCacheRepository.clearCache("UC123") } returns Unit
        coEvery { channelVideoCacheRepository.cacheVideos("UC123", any()) } coAnswers {
            val videos = secondArg<List<PlaylistVideo>>()
            cachedVideosFlow.value = cachedVideosFlow.value + videos
        }
    }

    @After
    fun tearDown() {
        cachedVideosFlow.value = emptyList()
        searchResultsFlow.value = emptyList()
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        channelId: String = "UC123",
        channelTitle: String = "Fun Channel"
    ): ChannelDetailViewModel {
        return ChannelDetailViewModel(
            youTubeApiRepository = youTubeApiRepository,
            channelVideoCacheRepository = channelVideoCacheRepository,
            channelId = channelId,
            channelTitle = channelTitle
        )
    }

    @Test
    fun `initial state is loading with channel title`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", null) } returns AppResult.Success(
            PaginatedPlaylistResult(emptyList(), null)
        )

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.isLoading).isTrue()
        assertThat(viewModel.uiState.value.channelTitle).isEqualTo("Fun Channel")
    }

    @Test
    fun `loads videos from channel uploads playlist`() = runTest(testDispatcher) {
        val videos = listOf(makeVideo("v1", "Video 1", 0), makeVideo("v2", "Video 2", 1))
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", null) } returns AppResult.Success(
            PaginatedPlaylistResult(videos, null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).hasSize(2)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `empty list when channel has no videos`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", null) } returns AppResult.Success(
            PaginatedPlaylistResult(emptyList(), null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).isEmpty()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `error state when channel fetch fails`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Error("API error: 404")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isEqualTo("API error: 404")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `error state when playlist fetch fails`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", null) } returns AppResult.Error("Network error")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isEqualTo("Network error")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `error when uploads playlist is null`() = runTest(testDispatcher) {
        val channelNoUploads = testChannel.copy(uploadsPlaylistId = null)
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(channelNoUploads)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).contains("uploads playlist not found")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `retry reloads videos`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Error("Network error")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isNotNull()

        val videos = listOf(makeVideo("v1", "Video 1", 0))
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", null) } returns AppResult.Success(
            PaginatedPlaylistResult(videos, null)
        )

        viewModel.retry()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).hasSize(1)
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `hasMorePages true when nextPageToken present`() = runTest(testDispatcher) {
        val videos = listOf(makeVideo("v1", "Video 1", 0))
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", null) } returns AppResult.Success(
            PaginatedPlaylistResult(videos, "PAGE2_TOKEN")
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.hasMorePages).isTrue()
    }

    @Test
    fun `loadMore fetches next page and appends to cache`() = runTest(testDispatcher) {
        val page1Videos = listOf(makeVideo("v1", "Video 1", 0))
        val page2Videos = listOf(makeVideo("v2", "Video 2", 1))
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", null) } returns AppResult.Success(
            PaginatedPlaylistResult(page1Videos, "PAGE2_TOKEN")
        )
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", "PAGE2_TOKEN") } returns AppResult.Success(
            PaginatedPlaylistResult(page2Videos, null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).hasSize(1)
        assertThat(viewModel.uiState.value.hasMorePages).isTrue()

        viewModel.loadMore()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).hasSize(2)
        assertThat(viewModel.uiState.value.hasMorePages).isFalse()
        assertThat(viewModel.uiState.value.isLoadingMore).isFalse()
    }

    @Test
    fun `loadMore does nothing when no more pages`() = runTest(testDispatcher) {
        val videos = listOf(makeVideo("v1", "Video 1", 0))
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", null) } returns AppResult.Success(
            PaginatedPlaylistResult(videos, null)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        coVerify(exactly = 1) { youTubeApiRepository.getPlaylistItemsPage(any(), any()) }
    }

    @Test
    fun `search query filters cached videos via Room`() = runTest(testDispatcher) {
        val videos = listOf(makeVideo("v1", "Fun Video", 0), makeVideo("v2", "Boring Video", 1))
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", null) } returns AppResult.Success(
            PaginatedPlaylistResult(videos, null)
        )

        searchResultsFlow.value = listOf(makeVideo("v1", "Fun Video", 0))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).hasSize(2)

        viewModel.onSearchQueryChanged("Fun")
        advanceTimeBy(301)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).hasSize(1)
        assertThat(viewModel.uiState.value.videos.first().title).isEqualTo("Fun Video")
    }

    @Test
    fun `clear search shows all cached videos`() = runTest(testDispatcher) {
        val videos = listOf(makeVideo("v1", "Fun Video", 0), makeVideo("v2", "Boring Video", 1))
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", null) } returns AppResult.Success(
            PaginatedPlaylistResult(videos, null)
        )

        searchResultsFlow.value = listOf(makeVideo("v1", "Fun Video", 0))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Fun")
        advanceTimeBy(301)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.videos).hasSize(1)

        viewModel.onClearSearch()
        advanceTimeBy(301)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).hasSize(2)
    }

    @Test
    fun `loadMore error keeps existing videos and shows error`() = runTest(testDispatcher) {
        val page1Videos = listOf(makeVideo("v1", "Video 1", 0))
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(testChannel)
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", null) } returns AppResult.Success(
            PaginatedPlaylistResult(page1Videos, "PAGE2_TOKEN")
        )
        coEvery { youTubeApiRepository.getPlaylistItemsPage("UU123", "PAGE2_TOKEN") } returns AppResult.Error("Network error")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).hasSize(1)
        assertThat(viewModel.uiState.value.error).isEqualTo("Network error")
        assertThat(viewModel.uiState.value.isLoadingMore).isFalse()
    }
}
