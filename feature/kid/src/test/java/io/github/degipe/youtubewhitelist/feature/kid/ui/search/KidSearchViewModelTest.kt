package io.github.degipe.youtubewhitelist.feature.kid.ui.search

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(profileId: String = "profile-1"): KidSearchViewModel {
        return KidSearchViewModel(
            whitelistRepository = whitelistRepository,
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

}
