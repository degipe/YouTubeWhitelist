package io.github.degipe.youtubewhitelist.feature.kid.ui.search

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.model.PlaylistVideo
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KidSearchViewModelTest {

    private lateinit var whitelistRepository: WhitelistRepository
    private lateinit var youTubeApiRepository: YouTubeApiRepository
    private val testDispatcher = StandardTestDispatcher()

    private val testVideo = WhitelistItem(
        id = "wl-1", kidProfileId = "profile-1",
        type = WhitelistItemType.VIDEO, youtubeId = "vid123",
        title = "Fun Video", thumbnailUrl = "https://img/vid1.jpg",
        channelTitle = "Fun Channel", addedAt = 1000L
    )

    private val testChannel = WhitelistItem(
        id = "wl-2", kidProfileId = "profile-1",
        type = WhitelistItemType.CHANNEL, youtubeId = "UC123",
        title = "Fun Channel", thumbnailUrl = "https://img/ch1.jpg",
        channelTitle = null, addedAt = 2000L
    )

    private val testPlaylist = WhitelistItem(
        id = "wl-3", kidProfileId = "profile-1",
        type = WhitelistItemType.PLAYLIST, youtubeId = "PL123",
        title = "Fun Playlist", thumbnailUrl = "https://img/pl1.jpg",
        channelTitle = "Fun Channel", addedAt = 3000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whitelistRepository = mockk()
        youTubeApiRepository = mockk()
        // Default: no whitelisted channels (existing tests don't need API search)
        coEvery { whitelistRepository.getChannelYoutubeIds(any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(profileId: String = "profile-1"): KidSearchViewModel {
        return KidSearchViewModel(
            whitelistRepository = whitelistRepository,
            youTubeApiRepository = youTubeApiRepository,
            profileId = profileId
        )
    }

    @Test
    fun `initial state has empty query and no results`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        with(viewModel.uiState.value) {
            assertThat(query).isEmpty()
            assertThat(results).isEmpty()
            assertThat(isSearching).isFalse()
        }
    }

    @Test
    fun `query update triggers search after debounce`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems("profile-1", "fun") } returns flowOf(listOf(testVideo))

        val viewModel = createViewModel()
        viewModel.onQueryChanged("fun")

        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.query).isEqualTo("fun")
        assertThat(viewModel.uiState.value.results).hasSize(1)
        assertThat(viewModel.uiState.value.results[0].title).isEqualTo("Fun Video")
    }

    @Test
    fun `search does not trigger before debounce`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems("profile-1", "fun") } returns flowOf(listOf(testVideo))

        val viewModel = createViewModel()
        viewModel.onQueryChanged("fun")

        advanceTimeBy(100)
        // Do NOT call advanceUntilIdle() - it would advance past debounce

        assertThat(viewModel.uiState.value.results).isEmpty()
    }

    @Test
    fun `rapid typing only triggers search for last query`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems("profile-1", "fun") } returns flowOf(listOf(testVideo))
        every { whitelistRepository.searchItems("profile-1", "funny") } returns flowOf(listOf(testVideo, testChannel))

        val viewModel = createViewModel()
        viewModel.onQueryChanged("fun")
        advanceTimeBy(100)
        viewModel.onQueryChanged("funny")

        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.query).isEqualTo("funny")
        assertThat(viewModel.uiState.value.results).hasSize(2)
        verify(exactly = 0) { whitelistRepository.searchItems("profile-1", "fun") }
    }

    @Test
    fun `empty query clears results without searching`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems("profile-1", "fun") } returns flowOf(listOf(testVideo))

        val viewModel = createViewModel()
        viewModel.onQueryChanged("fun")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.results).hasSize(1)

        viewModel.onQueryChanged("")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.results).isEmpty()
        assertThat(viewModel.uiState.value.query).isEmpty()
    }

    @Test
    fun `blank query clears results without searching`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems("profile-1", "fun") } returns flowOf(listOf(testVideo))

        val viewModel = createViewModel()
        viewModel.onQueryChanged("fun")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onQueryChanged("   ")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.results).isEmpty()
    }

    @Test
    fun `onClearQuery resets query and results`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems("profile-1", "fun") } returns flowOf(listOf(testVideo))

        val viewModel = createViewModel()
        viewModel.onQueryChanged("fun")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onClearQuery()
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.query).isEmpty()
        assertThat(viewModel.uiState.value.results).isEmpty()
    }

    @Test
    fun `search returns mixed content types`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems("profile-1", "fun") } returns
            flowOf(listOf(testChannel, testVideo, testPlaylist))

        val viewModel = createViewModel()
        viewModel.onQueryChanged("fun")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.results).hasSize(3)
        assertThat(viewModel.uiState.value.results.map { it.type }).containsExactly(
            WhitelistItemType.CHANNEL, WhitelistItemType.VIDEO, WhitelistItemType.PLAYLIST
        )
    }

    @Test
    fun `search with no matches returns empty results`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems("profile-1", "xyz") } returns flowOf(emptyList())

        val viewModel = createViewModel()
        viewModel.onQueryChanged("xyz")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.results).isEmpty()
        assertThat(viewModel.uiState.value.query).isEqualTo("xyz")
    }

    @Test
    fun `uses correct profileId for search`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems("profile-2", "fun") } returns flowOf(listOf(testVideo))

        val viewModel = createViewModel(profileId = "profile-2")
        viewModel.onQueryChanged("fun")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        verify { whitelistRepository.searchItems("profile-2", "fun") }
    }

    @Test
    fun `query StateFlow updates immediately without debounce`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems(any(), any()) } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.onQueryChanged("hello")
        // No advanceTimeBy — query should be immediate
        assertThat(viewModel.query.value).isEqualTo("hello")
    }

    @Test
    fun `rapid typing keeps query in sync`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems(any(), any()) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        viewModel.onQueryChanged("a")
        assertThat(viewModel.query.value).isEqualTo("a")

        viewModel.onQueryChanged("ab")
        assertThat(viewModel.query.value).isEqualTo("ab")

        viewModel.onQueryChanged("abc")
        assertThat(viewModel.query.value).isEqualTo("abc")
    }

    // === Channel video search via YouTube API ===

    @Test
    fun `search includes videos from whitelisted channels via API`() = runTest(testDispatcher) {
        // Local search returns the channel itself
        every { whitelistRepository.searchItems("profile-1", "fun") } returns flowOf(listOf(testChannel))
        coEvery { whitelistRepository.getChannelYoutubeIds("profile-1") } returns listOf("UC123")
        coEvery { youTubeApiRepository.searchVideosInChannel("UC123", "fun") } returns
            AppResult.Success(listOf(
                PlaylistVideo(videoId = "vid-api-1", title = "Dog having Fun", thumbnailUrl = "https://img/api1.jpg", channelTitle = "Fun Channel", position = 0)
            ))

        val viewModel = createViewModel()
        viewModel.onQueryChanged("fun")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        val results = viewModel.uiState.value.results
        assertThat(results).hasSize(2) // channel + API video
        assertThat(results.map { it.title }).containsAtLeast("Fun Channel", "Dog having Fun")
    }

    @Test
    fun `channel video search does not duplicate already whitelisted videos`() = runTest(testDispatcher) {
        // Local search returns a video with youtubeId "vid123"
        every { whitelistRepository.searchItems("profile-1", "fun") } returns flowOf(listOf(testVideo))
        coEvery { whitelistRepository.getChannelYoutubeIds("profile-1") } returns listOf("UC123")
        // API returns same videoId "vid123"
        coEvery { youTubeApiRepository.searchVideosInChannel("UC123", "fun") } returns
            AppResult.Success(listOf(
                PlaylistVideo(videoId = "vid123", title = "Fun Video", thumbnailUrl = "https://img/v.jpg", channelTitle = "Fun Channel", position = 0)
            ))

        val viewModel = createViewModel()
        viewModel.onQueryChanged("fun")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        val results = viewModel.uiState.value.results
        assertThat(results).hasSize(1) // deduplicated
        assertThat(results[0].youtubeId).isEqualTo("vid123")
    }

    @Test
    fun `channel video search skips API when no channels whitelisted`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems("profile-1", "fun") } returns flowOf(listOf(testVideo))
        coEvery { whitelistRepository.getChannelYoutubeIds("profile-1") } returns emptyList()

        val viewModel = createViewModel()
        viewModel.onQueryChanged("fun")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { youTubeApiRepository.searchVideosInChannel(any(), any()) }
        assertThat(viewModel.uiState.value.results).hasSize(1)
    }

    @Test
    fun `channel video search handles API error gracefully`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems("profile-1", "fun") } returns flowOf(listOf(testChannel))
        coEvery { whitelistRepository.getChannelYoutubeIds("profile-1") } returns listOf("UC123")
        coEvery { youTubeApiRepository.searchVideosInChannel("UC123", "fun") } returns
            AppResult.Error("Network error")

        val viewModel = createViewModel()
        viewModel.onQueryChanged("fun")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should still show local results despite API error
        val results = viewModel.uiState.value.results
        assertThat(results).hasSize(1)
        assertThat(results[0].title).isEqualTo("Fun Channel")
    }

    @Test
    fun `channel video search limits to 3 channels max`() = runTest(testDispatcher) {
        every { whitelistRepository.searchItems("profile-1", "fun") } returns flowOf(emptyList())
        coEvery { whitelistRepository.getChannelYoutubeIds("profile-1") } returns
            listOf("UC1", "UC2", "UC3", "UC4", "UC5")
        coEvery { youTubeApiRepository.searchVideosInChannel(any(), any()) } returns
            AppResult.Success(emptyList())

        val viewModel = createViewModel()
        viewModel.onQueryChanged("fun")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should only call for first 3 channels (quota protection)
        coVerify(exactly = 1) { youTubeApiRepository.searchVideosInChannel("UC1", "fun") }
        coVerify(exactly = 1) { youTubeApiRepository.searchVideosInChannel("UC2", "fun") }
        coVerify(exactly = 1) { youTubeApiRepository.searchVideosInChannel("UC3", "fun") }
        coVerify(exactly = 0) { youTubeApiRepository.searchVideosInChannel("UC4", any()) }
        coVerify(exactly = 0) { youTubeApiRepository.searchVideosInChannel("UC5", any()) }
    }
}
