package io.github.degipe.youtubewhitelist.feature.sleep.ui

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import io.mockk.every
import io.mockk.mockk
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
class SleepModeViewModelTest {

    private lateinit var whitelistRepository: WhitelistRepository
    private val testDispatcher = StandardTestDispatcher()

    private val video1 = WhitelistItem(
        id = "wl-1", kidProfileId = "profile-1",
        type = WhitelistItemType.VIDEO, youtubeId = "vid1",
        title = "Lullaby 1", thumbnailUrl = "https://img/1.jpg",
        channelTitle = "Sleep Sounds", addedAt = 1000L
    )

    private val video2 = WhitelistItem(
        id = "wl-2", kidProfileId = "profile-1",
        type = WhitelistItemType.VIDEO, youtubeId = "vid2",
        title = "Lullaby 2", thumbnailUrl = "https://img/2.jpg",
        channelTitle = "Sleep Sounds", addedAt = 2000L
    )

    private val video3 = WhitelistItem(
        id = "wl-3", kidProfileId = "profile-1",
        type = WhitelistItemType.VIDEO, youtubeId = "vid3",
        title = "Lullaby 3", thumbnailUrl = "https://img/3.jpg",
        channelTitle = "Sleep Sounds", addedAt = 3000L
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

    private fun createViewModel(profileId: String = "profile-1"): SleepModeViewModel {
        return SleepModeViewModel(
            whitelistRepository = whitelistRepository,
            profileId = profileId
        )
    }

    /** Create VM and advance so init's loadVideos() completes */
    private fun createViewModelAndLoad(profileId: String = "profile-1"): SleepModeViewModel {
        val vm = createViewModel(profileId)
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `initial state is selecting with default 30 minutes`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1, video2))

        val viewModel = createViewModelAndLoad()

        with(viewModel.uiState.value) {
            assertThat(timerState).isEqualTo(TimerState.SELECTING)
            assertThat(selectedDurationMinutes).isEqualTo(30)
            assertThat(remainingSeconds).isEqualTo(0)
            assertThat(fadeVolume).isEqualTo(1f)
        }
    }

    @Test
    fun `loads videos from profile`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1, video2))

        val viewModel = createViewModelAndLoad()

        assertThat(viewModel.uiState.value.videos).hasSize(2)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `selectDuration updates selected duration`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1))

        val viewModel = createViewModelAndLoad()
        viewModel.selectDuration(45)

        assertThat(viewModel.uiState.value.selectedDurationMinutes).isEqualTo(45)
    }

    @Test
    fun `startTimer transitions to running state`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1))

        val viewModel = createViewModelAndLoad()
        viewModel.selectDuration(15)
        viewModel.startTimer()
        // State is set synchronously before timer coroutine starts

        with(viewModel.uiState.value) {
            assertThat(timerState).isEqualTo(TimerState.RUNNING)
            assertThat(remainingSeconds).isEqualTo(15 * 60L)
        }
    }

    @Test
    fun `timer counts down every second`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1))

        val viewModel = createViewModelAndLoad()
        viewModel.selectDuration(15)
        viewModel.startTimer()

        // advanceTimeBy is exclusive at the boundary, so +1ms to ensure the 5th tick
        advanceTimeBy(5_001)

        assertThat(viewModel.uiState.value.remainingSeconds).isEqualTo(15 * 60L - 5)
    }

    @Test
    fun `timer expires after full duration`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1))

        val viewModel = createViewModelAndLoad()
        viewModel.selectDuration(15)
        viewModel.startTimer()

        advanceTimeBy(15 * 60 * 1000L + 1000)

        assertThat(viewModel.uiState.value.timerState).isEqualTo(TimerState.EXPIRED)
        assertThat(viewModel.uiState.value.remainingSeconds).isEqualTo(0)
    }

    @Test
    fun `fadeVolume is 1 when more than 2 minutes remaining`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1))

        val viewModel = createViewModelAndLoad()
        viewModel.selectDuration(15)
        viewModel.startTimer()

        // Advance to 3 minutes remaining
        advanceTimeBy((15 * 60 - 180) * 1000L)

        assertThat(viewModel.uiState.value.fadeVolume).isEqualTo(1f)
    }

    @Test
    fun `fadeVolume decreases in last 2 minutes`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1))

        val viewModel = createViewModelAndLoad()
        viewModel.selectDuration(15)
        viewModel.startTimer()

        // Advance to 60 seconds remaining (half of fade duration)
        advanceTimeBy((15 * 60 - 60) * 1000L)

        assertThat(viewModel.uiState.value.fadeVolume).isWithin(0.01f).of(0.5f)
    }

    @Test
    fun `fadeVolume approaches 0 near timer end`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1))

        val viewModel = createViewModelAndLoad()
        viewModel.selectDuration(15)
        viewModel.startTimer()

        // Advance to 5 seconds remaining
        advanceTimeBy((15 * 60 - 5) * 1000L)

        assertThat(viewModel.uiState.value.fadeVolume).isLessThan(0.1f)
    }

    @Test
    fun `stopTimer resets to selecting state`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1))

        val viewModel = createViewModelAndLoad()
        viewModel.startTimer()

        advanceTimeBy(5_000)

        viewModel.stopTimer()

        with(viewModel.uiState.value) {
            assertThat(timerState).isEqualTo(TimerState.SELECTING)
            assertThat(remainingSeconds).isEqualTo(0)
            assertThat(fadeVolume).isEqualTo(1f)
        }
    }

    @Test
    fun `onVideoEnded advances to next video`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1, video2, video3))

        val viewModel = createViewModelAndLoad()
        viewModel.startTimer()
        // Timer coroutine is launched but not yet ticking (StandardTestDispatcher)

        assertThat(viewModel.uiState.value.currentVideoIndex).isEqualTo(0)

        viewModel.onVideoEnded()

        assertThat(viewModel.uiState.value.currentVideoIndex).isEqualTo(1)
    }

    @Test
    fun `onVideoEnded wraps around to first video`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1, video2))

        val viewModel = createViewModelAndLoad()
        viewModel.startTimer()

        viewModel.onVideoEnded() // -> index 1
        viewModel.onVideoEnded() // -> wraps to 0

        assertThat(viewModel.uiState.value.currentVideoIndex).isEqualTo(0)
    }

    @Test
    fun `onVideoEnded does nothing when timer expired`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1, video2))

        val viewModel = createViewModelAndLoad()
        viewModel.selectDuration(15)
        viewModel.startTimer()

        // Expire timer
        advanceTimeBy(15 * 60 * 1000L + 1000)

        val indexBefore = viewModel.uiState.value.currentVideoIndex
        viewModel.onVideoEnded()

        assertThat(viewModel.uiState.value.currentVideoIndex).isEqualTo(indexBefore)
    }

    @Test
    fun `currentVideo returns correct video`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(video1, video2))

        val viewModel = createViewModelAndLoad()
        viewModel.startTimer()

        assertThat(viewModel.uiState.value.currentVideo?.youtubeId).isEqualTo("vid1")

        viewModel.onVideoEnded()

        assertThat(viewModel.uiState.value.currentVideo?.youtubeId).isEqualTo("vid2")
    }

    @Test
    fun `empty videos shows no video`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())

        val viewModel = createViewModelAndLoad()

        assertThat(viewModel.uiState.value.videos).isEmpty()
        assertThat(viewModel.uiState.value.currentVideo).isNull()
    }
}
