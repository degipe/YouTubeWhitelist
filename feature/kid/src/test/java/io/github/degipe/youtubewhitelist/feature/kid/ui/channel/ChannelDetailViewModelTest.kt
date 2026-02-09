package io.github.degipe.youtubewhitelist.feature.kid.ui.channel

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChannelDetailViewModelTest {

    private lateinit var whitelistRepository: WhitelistRepository
    private val testDispatcher = StandardTestDispatcher()

    private fun makeVideo(id: String, title: String, addedAt: Long = 1000L) = WhitelistItem(
        id = id, kidProfileId = "profile-1",
        type = WhitelistItemType.VIDEO, youtubeId = "yt-$id",
        title = title, thumbnailUrl = "https://img/$id.jpg",
        channelTitle = "Fun Channel", addedAt = addedAt
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

    private fun createViewModel(
        profileId: String = "profile-1",
        channelTitle: String = "Fun Channel"
    ): ChannelDetailViewModel {
        return ChannelDetailViewModel(
            whitelistRepository = whitelistRepository,
            profileId = profileId,
            channelTitle = channelTitle
        )
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(emptyList())

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun `channel title set from parameter`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(emptyList())

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.channelTitle).isEqualTo("Fun Channel")
    }

    @Test
    fun `loads videos by channel title`() = runTest(testDispatcher) {
        val videos = listOf(makeVideo("v1", "Video 1"), makeVideo("v2", "Video 2"))
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(videos)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).hasSize(2)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `empty list when no videos`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(emptyList())

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).isEmpty()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `reacts to flow updates`() = runTest(testDispatcher) {
        val videosFlow = MutableStateFlow<List<WhitelistItem>>(emptyList())
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns videosFlow

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).isEmpty()

        videosFlow.value = listOf(makeVideo("v1", "Video 1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).hasSize(1)
    }

    @Test
    fun `uses correct profileId and channelTitle`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-2", "Other Channel") } returns flowOf(emptyList())

        val viewModel = createViewModel(profileId = "profile-2", channelTitle = "Other Channel")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.channelTitle).isEqualTo("Other Channel")
    }
}
