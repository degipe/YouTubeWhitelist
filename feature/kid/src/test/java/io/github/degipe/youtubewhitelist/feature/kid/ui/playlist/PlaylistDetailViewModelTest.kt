package io.github.degipe.youtubewhitelist.feature.kid.ui.playlist

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.model.PlaylistVideo
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class PlaylistDetailViewModelTest {

    private lateinit var youTubeApiRepository: YouTubeApiRepository
    private val testDispatcher = StandardTestDispatcher()

    private fun makeVideo(videoId: String, title: String, position: Int = 0) = PlaylistVideo(
        videoId = videoId,
        title = title,
        thumbnailUrl = "https://img/$videoId.jpg",
        channelTitle = "Test Channel",
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
        profileId: String = "profile-1",
        playlistId: String = "PL123"
    ): PlaylistDetailViewModel {
        return PlaylistDetailViewModel(
            youTubeApiRepository = youTubeApiRepository,
            profileId = profileId,
            playlistId = playlistId
        )
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getPlaylistItems("PL123") } returns
            AppResult.Success(emptyList())

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.isLoading).isTrue()
        assertThat(viewModel.uiState.value.videos).isEmpty()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun `loads playlist items on init`() = runTest(testDispatcher) {
        val videos = listOf(
            makeVideo("vid1", "Video 1", 0),
            makeVideo("vid2", "Video 2", 1)
        )
        coEvery { youTubeApiRepository.getPlaylistItems("PL123") } returns
            AppResult.Success(videos)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.videos).hasSize(2)
        assertThat(viewModel.uiState.value.videos[0].videoId).isEqualTo("vid1")
        assertThat(viewModel.uiState.value.videos[1].videoId).isEqualTo("vid2")
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun `empty playlist shows empty state`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getPlaylistItems("PL_empty") } returns
            AppResult.Success(emptyList())

        val viewModel = createViewModel(playlistId = "PL_empty")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.videos).isEmpty()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun `API error shows error state`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getPlaylistItems("PL123") } returns
            AppResult.Error("API error: 403")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.videos).isEmpty()
        assertThat(viewModel.uiState.value.errorMessage).isEqualTo("API error: 403")
    }

    @Test
    fun `retry reloads playlist items`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getPlaylistItems("PL123") } returns
            AppResult.Error("Network error") andThen
            AppResult.Success(listOf(makeVideo("vid1", "Video 1")))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isNotNull()

        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.videos).hasSize(1)
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun `retry shows loading state`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getPlaylistItems("PL123") } returns
            AppResult.Error("error")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { youTubeApiRepository.getPlaylistItems("PL123") } returns
            AppResult.Success(emptyList())

        viewModel.retry()

        assertThat(viewModel.uiState.value.isLoading).isTrue()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun `uses correct playlistId`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getPlaylistItems("PL_other") } returns
            AppResult.Success(emptyList())

        createViewModel(playlistId = "PL_other")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { youTubeApiRepository.getPlaylistItems("PL_other") }
    }

    @Test
    fun `videos sorted by position`() = runTest(testDispatcher) {
        val videos = listOf(
            makeVideo("vid3", "Video 3", 2),
            makeVideo("vid1", "Video 1", 0),
            makeVideo("vid2", "Video 2", 1)
        )
        coEvery { youTubeApiRepository.getPlaylistItems("PL123") } returns
            AppResult.Success(videos)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos[0].videoId).isEqualTo("vid1")
        assertThat(viewModel.uiState.value.videos[1].videoId).isEqualTo("vid2")
        assertThat(viewModel.uiState.value.videos[2].videoId).isEqualTo("vid3")
    }
}
