package io.github.degipe.youtubewhitelist.feature.parent.ui.browser

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.common.youtube.YouTubeContentType
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
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
class WebViewBrowserViewModelTest {

    private lateinit var whitelistRepository: WhitelistRepository
    private lateinit var viewModel: WebViewBrowserViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val profileId = "profile-123"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whitelistRepository = mockk(relaxed = true)
        viewModel = WebViewBrowserViewModel(whitelistRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Initial State ---

    @Test
    fun `initial state has youtube url and no detection`() {
        val state = viewModel.uiState.value
        assertThat(state.currentUrl).isEqualTo("https://www.youtube.com")
        assertThat(state.detectedContent).isNull()
        assertThat(state.isAdding).isFalse()
        assertThat(state.addResult).isNull()
    }

    // --- URL Detection ---

    @Test
    fun `onUrlChanged detects video URL`() {
        viewModel.onUrlChanged("https://www.youtube.com/watch?v=dQw4w9WgXcQ")

        val state = viewModel.uiState.value
        assertThat(state.currentUrl).isEqualTo("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        assertThat(state.detectedContent).isNotNull()
        assertThat(state.detectedContent!!.type).isEqualTo(YouTubeContentType.VIDEO)
        assertThat(state.detectedContent!!.id).isEqualTo("dQw4w9WgXcQ")
    }

    @Test
    fun `onUrlChanged detects channel URL`() {
        viewModel.onUrlChanged("https://www.youtube.com/channel/UC123456")

        val state = viewModel.uiState.value
        assertThat(state.detectedContent).isNotNull()
        assertThat(state.detectedContent!!.type).isEqualTo(YouTubeContentType.CHANNEL)
    }

    @Test
    fun `onUrlChanged detects handle URL`() {
        viewModel.onUrlChanged("https://www.youtube.com/@CoolCreator")

        val state = viewModel.uiState.value
        assertThat(state.detectedContent).isNotNull()
        assertThat(state.detectedContent!!.type).isEqualTo(YouTubeContentType.CHANNEL_HANDLE)
    }

    @Test
    fun `onUrlChanged detects playlist URL`() {
        viewModel.onUrlChanged("https://www.youtube.com/playlist?list=PLtest123")

        val state = viewModel.uiState.value
        assertThat(state.detectedContent).isNotNull()
        assertThat(state.detectedContent!!.type).isEqualTo(YouTubeContentType.PLAYLIST)
    }

    @Test
    fun `onUrlChanged clears detection for non-youtube URL`() {
        viewModel.onUrlChanged("https://www.youtube.com/watch?v=test")
        assertThat(viewModel.uiState.value.detectedContent).isNotNull()

        viewModel.onUrlChanged("https://www.google.com")
        assertThat(viewModel.uiState.value.detectedContent).isNull()
    }

    @Test
    fun `onUrlChanged clears detection for youtube homepage`() {
        viewModel.onUrlChanged("https://www.youtube.com/watch?v=test")
        assertThat(viewModel.uiState.value.detectedContent).isNotNull()

        viewModel.onUrlChanged("https://www.youtube.com")
        assertThat(viewModel.uiState.value.detectedContent).isNull()
    }

    // --- Add to Whitelist ---

    @Test
    fun `addToWhitelist success sets result`() = runTest(testDispatcher) {
        val addedItem = WhitelistItem(
            id = "1", kidProfileId = profileId,
            type = WhitelistItemType.VIDEO, youtubeId = "test123",
            title = "Test Video", thumbnailUrl = "https://img.youtube.com/1.jpg",
            channelTitle = "Test Channel", addedAt = 1000L
        )
        coEvery {
            whitelistRepository.addItemFromUrl(profileId, "https://www.youtube.com/watch?v=test123")
        } returns AppResult.Success(addedItem)

        viewModel.onUrlChanged("https://www.youtube.com/watch?v=test123")
        viewModel.addToWhitelist(profileId)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isAdding).isFalse()
        assertThat(state.addResult).isNotNull()
        assertThat(state.addResult).isInstanceOf(AddToWhitelistResult.Success::class.java)
        assertThat((state.addResult as AddToWhitelistResult.Success).itemTitle).isEqualTo("Test Video")
    }

    @Test
    fun `addToWhitelist shows loading state`() = runTest(testDispatcher) {
        coEvery {
            whitelistRepository.addItemFromUrl(any(), any())
        } returns AppResult.Success(mockk(relaxed = true))

        viewModel.onUrlChanged("https://www.youtube.com/watch?v=test123")
        viewModel.addToWhitelist(profileId)

        assertThat(viewModel.uiState.value.isAdding).isTrue()

        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.isAdding).isFalse()
    }

    @Test
    fun `addToWhitelist error sets error result`() = runTest(testDispatcher) {
        coEvery {
            whitelistRepository.addItemFromUrl(profileId, "https://www.youtube.com/watch?v=bad")
        } returns AppResult.Error("Not found")

        viewModel.onUrlChanged("https://www.youtube.com/watch?v=bad")
        viewModel.addToWhitelist(profileId)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.addResult).isInstanceOf(AddToWhitelistResult.Error::class.java)
        assertThat((state.addResult as AddToWhitelistResult.Error).message).isEqualTo("Not found")
    }

    @Test
    fun `addToWhitelist does nothing when no content detected`() = runTest(testDispatcher) {
        viewModel.onUrlChanged("https://www.google.com")
        viewModel.addToWhitelist(profileId)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { whitelistRepository.addItemFromUrl(any(), any()) }
    }

    // --- Result Dismissal ---

    @Test
    fun `dismissResult clears add result`() = runTest(testDispatcher) {
        coEvery {
            whitelistRepository.addItemFromUrl(any(), any())
        } returns AppResult.Success(mockk(relaxed = true))

        viewModel.onUrlChanged("https://www.youtube.com/watch?v=test")
        viewModel.addToWhitelist(profileId)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.dismissResult()
        assertThat(viewModel.uiState.value.addResult).isNull()
    }
}
