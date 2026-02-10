package io.github.degipe.youtubewhitelist.feature.kid.ui.player

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.repository.WatchHistoryRepository
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerManager
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerState
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerStatus
import io.github.degipe.youtubewhitelist.core.data.timelimit.TimeLimitChecker
import io.github.degipe.youtubewhitelist.core.data.timelimit.TimeLimitStatus
import io.mockk.coEvery
import io.mockk.coVerify
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
class VideoPlayerViewModelTest {

    private lateinit var whitelistRepository: WhitelistRepository
    private lateinit var watchHistoryRepository: WatchHistoryRepository
    private lateinit var timeLimitChecker: TimeLimitChecker
    private lateinit var sleepTimerManager: SleepTimerManager
    private val sleepTimerStateFlow = MutableStateFlow(SleepTimerState())
    private val testDispatcher = StandardTestDispatcher()

    private val noLimitStatus = TimeLimitStatus(
        dailyLimitMinutes = null,
        watchedTodaySeconds = 0,
        remainingSeconds = null,
        isLimitReached = false
    )

    private fun makeVideo(id: String, title: String, channelTitle: String = "Fun Channel") = WhitelistItem(
        id = id, kidProfileId = "profile-1",
        type = WhitelistItemType.VIDEO, youtubeId = "yt-$id",
        title = title, thumbnailUrl = "https://img/$id.jpg",
        channelTitle = channelTitle, addedAt = 1000L
    )

    private val currentVideo = makeVideo("current", "Current Video")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whitelistRepository = mockk(relaxed = true)
        watchHistoryRepository = mockk(relaxed = true)
        timeLimitChecker = mockk()
        sleepTimerManager = mockk()
        every { sleepTimerManager.state } returns sleepTimerStateFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupDefaultTimeLimit() {
        every { timeLimitChecker.getTimeLimitStatus("profile-1") } returns flowOf(noLimitStatus)
    }

    private fun createViewModel(
        profileId: String = "profile-1",
        videoId: String = "yt-current",
        videoTitle: String = "Current Video",
        channelTitle: String? = "Fun Channel"
    ): VideoPlayerViewModel {
        return VideoPlayerViewModel(
            whitelistRepository = whitelistRepository,
            watchHistoryRepository = watchHistoryRepository,
            timeLimitChecker = timeLimitChecker,
            sleepTimerManager = sleepTimerManager,
            profileId = profileId,
            videoId = videoId,
            initialVideoTitle = videoTitle,
            channelTitle = channelTitle
        )
    }

    @Test
    fun `initial state has video id and title`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(emptyList())
        setupDefaultTimeLimit()

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.videoId).isEqualTo("yt-current")
        assertThat(viewModel.uiState.value.videoTitle).isEqualTo("Current Video")
        assertThat(viewModel.uiState.value.youtubeId).isEqualTo("yt-current")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `loads sibling videos when channelTitle provided`() = runTest(testDispatcher) {
        val siblings = listOf(
            makeVideo("v1", "Video 1"),
            currentVideo,
            makeVideo("v3", "Video 3")
        )
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(siblings)
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.siblingVideos).hasSize(3)
    }

    @Test
    fun `no siblings when channelTitle is null`() = runTest(testDispatcher) {
        setupDefaultTimeLimit()

        val viewModel = createViewModel(channelTitle = null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.siblingVideos).isEmpty()
    }

    @Test
    fun `current index tracks position in sibling list`() = runTest(testDispatcher) {
        val siblings = listOf(
            makeVideo("v1", "Video 1"),
            currentVideo,
            makeVideo("v3", "Video 3")
        )
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(siblings)
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.currentIndex).isEqualTo(1)
    }

    @Test
    fun `hasNext is true when not last video`() = runTest(testDispatcher) {
        val siblings = listOf(currentVideo, makeVideo("v2", "Video 2"))
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(siblings)
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.hasNext).isTrue()
    }

    @Test
    fun `hasNext is false when last video`() = runTest(testDispatcher) {
        val siblings = listOf(makeVideo("v1", "Video 1"), currentVideo)
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(siblings)
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.hasNext).isFalse()
    }

    @Test
    fun `hasPrevious is true when not first video`() = runTest(testDispatcher) {
        val siblings = listOf(makeVideo("v1", "Video 1"), currentVideo)
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(siblings)
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.hasPrevious).isTrue()
    }

    @Test
    fun `hasPrevious is false when first video`() = runTest(testDispatcher) {
        val siblings = listOf(currentVideo, makeVideo("v2", "Video 2"))
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(siblings)
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.hasPrevious).isFalse()
    }

    @Test
    fun `onVideoEnded records watch history`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(listOf(currentVideo))
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onVideoEnded(watchedSeconds = 120)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            watchHistoryRepository.recordWatch("profile-1", "yt-current", "Current Video", 120)
        }
    }

    @Test
    fun `onVideoEnded auto-next when has next`() = runTest(testDispatcher) {
        val siblings = listOf(currentVideo, makeVideo("v2", "Next Video"))
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(siblings)
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onVideoEnded(watchedSeconds = 60)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.youtubeId).isEqualTo("yt-v2")
    }

    @Test
    fun `playNext navigates to next video`() = runTest(testDispatcher) {
        val siblings = listOf(currentVideo, makeVideo("v2", "Next Video"))
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(siblings)
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.playNext()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.youtubeId).isEqualTo("yt-v2")
    }

    @Test
    fun `playPrevious navigates to previous video`() = runTest(testDispatcher) {
        val siblings = listOf(makeVideo("v1", "Prev Video"), currentVideo)
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(siblings)
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.playPrevious()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.youtubeId).isEqualTo("yt-v1")
    }

    @Test
    fun `playNext does nothing when no next`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(listOf(currentVideo))
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.playNext()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.youtubeId).isEqualTo("yt-current")
    }

    // === Time Limit Tests ===

    @Test
    fun `displays remaining time when limit set`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(emptyList())
        every { timeLimitChecker.getTimeLimitStatus("profile-1") } returns flowOf(
            TimeLimitStatus(dailyLimitMinutes = 60, watchedTodaySeconds = 1800, remainingSeconds = 1800, isLimitReached = false)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.remainingTimeFormatted).isEqualTo("30m")
        assertThat(viewModel.uiState.value.isTimeLimitReached).isFalse()
    }

    @Test
    fun `time limit reached sets flag`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(emptyList())
        every { timeLimitChecker.getTimeLimitStatus("profile-1") } returns flowOf(
            TimeLimitStatus(dailyLimitMinutes = 60, watchedTodaySeconds = 3600, remainingSeconds = 0, isLimitReached = true)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isTimeLimitReached).isTrue()
    }

    @Test
    fun `no limit set shows no remaining time`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(emptyList())
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.remainingTimeFormatted).isNull()
        assertThat(viewModel.uiState.value.isTimeLimitReached).isFalse()
    }

    @Test
    fun `remaining time updates reactively`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(emptyList())
        every { timeLimitChecker.getTimeLimitStatus("profile-1") } returns flowOf(
            TimeLimitStatus(dailyLimitMinutes = 60, watchedTodaySeconds = 3000, remainingSeconds = 600, isLimitReached = false)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.remainingTimeFormatted).isEqualTo("10m")
    }

    // === Edge cases ===

    @Test
    fun `playPrevious does nothing on single video`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(listOf(currentVideo))
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.playPrevious()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.youtubeId).isEqualTo("yt-current")
        assertThat(viewModel.uiState.value.hasPrevious).isFalse()
        assertThat(viewModel.uiState.value.hasNext).isFalse()
    }

    @Test
    fun `onVideoEnded stays on last video when no next`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(listOf(currentVideo))
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onVideoEnded(watchedSeconds = 60)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.youtubeId).isEqualTo("yt-current")
        coVerify { watchHistoryRepository.recordWatch("profile-1", "yt-current", "Current Video", 60) }
    }

    @Test
    fun `playVideoAt ignores out of bounds index`() = runTest(testDispatcher) {
        val siblings = listOf(currentVideo, makeVideo("v2", "Video 2"))
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(siblings)
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.playVideoAt(5)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.youtubeId).isEqualTo("yt-current")
    }

    @Test
    fun `playVideoAt ignores negative index`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(listOf(currentVideo))
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.playVideoAt(-1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.youtubeId).isEqualTo("yt-current")
    }

    @Test
    fun `formats 1 hour remaining correctly`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(emptyList())
        every { timeLimitChecker.getTimeLimitStatus("profile-1") } returns flowOf(
            TimeLimitStatus(dailyLimitMinutes = 120, watchedTodaySeconds = 3600, remainingSeconds = 3600, isLimitReached = false)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.remainingTimeFormatted).isEqualTo("1h 0m")
    }

    // === Sleep Timer Tests ===

    @Test
    fun `sleep timer expired for this profile sets isSleepTimerExpired true`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(emptyList())
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        sleepTimerStateFlow.value = SleepTimerState(
            status = SleepTimerStatus.EXPIRED,
            profileId = "profile-1",
            remainingSeconds = 0
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isSleepTimerExpired).isTrue()
    }

    @Test
    fun `sleep timer running does not set isSleepTimerExpired`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(emptyList())
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        sleepTimerStateFlow.value = SleepTimerState(
            status = SleepTimerStatus.RUNNING,
            profileId = "profile-1",
            remainingSeconds = 600
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isSleepTimerExpired).isFalse()
    }

    @Test
    fun `sleep timer expired for different profile does not set isSleepTimerExpired`() = runTest(testDispatcher) {
        every { whitelistRepository.getVideosByChannelTitle("profile-1", "Fun Channel") } returns flowOf(emptyList())
        setupDefaultTimeLimit()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        sleepTimerStateFlow.value = SleepTimerState(
            status = SleepTimerStatus.EXPIRED,
            profileId = "profile-OTHER",
            remainingSeconds = 0
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isSleepTimerExpired).isFalse()
    }
}
