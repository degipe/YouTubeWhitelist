package io.github.degipe.youtubewhitelist.feature.kid.ui.home

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.data.model.KidProfile
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerManager
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerState
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerStatus
import io.github.degipe.youtubewhitelist.core.data.timelimit.TimeLimitChecker
import io.github.degipe.youtubewhitelist.core.data.timelimit.TimeLimitStatus
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
class KidHomeViewModelTest {

    private lateinit var whitelistRepository: WhitelistRepository
    private lateinit var kidProfileRepository: KidProfileRepository
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

    private val testChannel = WhitelistItem(
        id = "wl-1", kidProfileId = "profile-1",
        type = WhitelistItemType.CHANNEL, youtubeId = "UC123",
        title = "Fun Channel", thumbnailUrl = "https://img/ch1.jpg",
        channelTitle = null, addedAt = 1000L
    )

    private val testVideo = WhitelistItem(
        id = "wl-2", kidProfileId = "profile-1",
        type = WhitelistItemType.VIDEO, youtubeId = "vid123",
        title = "Cool Video", thumbnailUrl = "https://img/vid1.jpg",
        channelTitle = "Fun Channel", addedAt = 2000L
    )

    private val testPlaylist = WhitelistItem(
        id = "wl-3", kidProfileId = "profile-1",
        type = WhitelistItemType.PLAYLIST, youtubeId = "PL123",
        title = "My Playlist", thumbnailUrl = "https://img/pl1.jpg",
        channelTitle = "Fun Channel", addedAt = 3000L
    )

    private val testProfile = KidProfile(
        id = "profile-1", parentAccountId = "account-1",
        name = "Bence", avatarUrl = null,
        dailyLimitMinutes = null, sleepPlaylistId = null,
        createdAt = 1000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whitelistRepository = mockk()
        kidProfileRepository = mockk()
        timeLimitChecker = mockk()
        sleepTimerManager = mockk()
        every { sleepTimerManager.state } returns sleepTimerStateFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupDefaultTimeLimitChecker() {
        every { timeLimitChecker.getTimeLimitStatus("profile-1") } returns flowOf(noLimitStatus)
    }

    private fun createViewModel(profileId: String = "profile-1"): KidHomeViewModel {
        return KidHomeViewModel(
            whitelistRepository = whitelistRepository,
            kidProfileRepository = kidProfileRepository,
            timeLimitChecker = timeLimitChecker,
            sleepTimerManager = sleepTimerManager,
            profileId = profileId
        )
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun `loads profile name`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.profileName).isEqualTo("Bence")
    }

    @Test
    fun `loads channels`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(listOf(testChannel))
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.channels).hasSize(1)
        assertThat(viewModel.uiState.value.channels[0].title).isEqualTo("Fun Channel")
    }

    @Test
    fun `loads videos`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(testVideo))
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.recentVideos).hasSize(1)
        assertThat(viewModel.uiState.value.recentVideos[0].title).isEqualTo("Cool Video")
    }

    @Test
    fun `loads playlists`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(listOf(testPlaylist))
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.playlists).hasSize(1)
        assertThat(viewModel.uiState.value.playlists[0].title).isEqualTo("My Playlist")
    }

    @Test
    fun `empty state when no content`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isEmpty).isTrue()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `not empty when has content`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(listOf(testChannel))
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isEmpty).isFalse()
    }

    @Test
    fun `combines all content types`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(listOf(testChannel))
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(listOf(testVideo))
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(listOf(testPlaylist))
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertThat(channels).hasSize(1)
            assertThat(recentVideos).hasSize(1)
            assertThat(playlists).hasSize(1)
            assertThat(isEmpty).isFalse()
            assertThat(isLoading).isFalse()
        }
    }

    @Test
    fun `reacts to channel flow updates`() = runTest(testDispatcher) {
        val channelsFlow = MutableStateFlow<List<WhitelistItem>>(emptyList())
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns channelsFlow
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.channels).isEmpty()

        channelsFlow.value = listOf(testChannel)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.channels).hasSize(1)
    }

    @Test
    fun `profile name defaults to empty when profile is null`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(null)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.profileName).isEmpty()
    }

    // === Time Limit Tests ===

    @Test
    fun `shows remaining time when limit set`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        every { timeLimitChecker.getTimeLimitStatus("profile-1") } returns flowOf(
            TimeLimitStatus(dailyLimitMinutes = 60, watchedTodaySeconds = 1800, remainingSeconds = 1800, isLimitReached = false)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.remainingTimeFormatted).isEqualTo("30m")
        assertThat(viewModel.uiState.value.isTimeLimitReached).isFalse()
    }

    @Test
    fun `shows time limit reached`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        every { timeLimitChecker.getTimeLimitStatus("profile-1") } returns flowOf(
            TimeLimitStatus(dailyLimitMinutes = 60, watchedTodaySeconds = 3600, remainingSeconds = 0, isLimitReached = true)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isTimeLimitReached).isTrue()
    }

    @Test
    fun `no limit shows no remaining time`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.remainingTimeFormatted).isNull()
        assertThat(viewModel.uiState.value.isTimeLimitReached).isFalse()
    }

    // === Time Format Edge Cases ===

    @Test
    fun `formats exactly 1 hour remaining`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        every { timeLimitChecker.getTimeLimitStatus("profile-1") } returns flowOf(
            TimeLimitStatus(dailyLimitMinutes = 120, watchedTodaySeconds = 3600, remainingSeconds = 3600, isLimitReached = false)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.remainingTimeFormatted).isEqualTo("1h 0m")
    }

    @Test
    fun `formats less than 1 minute remaining as 0m`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        every { timeLimitChecker.getTimeLimitStatus("profile-1") } returns flowOf(
            TimeLimitStatus(dailyLimitMinutes = 60, watchedTodaySeconds = 3570, remainingSeconds = 30, isLimitReached = false)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.remainingTimeFormatted).isEqualTo("0m")
    }

    // === Sleep Timer Tests ===

    @Test
    fun `sleep timer expired for this profile sets isSleepTimerExpired true`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

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
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

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
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

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

    @Test
    fun `sleep timer IDLE does not set isSleepTimerExpired`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isSleepTimerExpired).isFalse()
    }

    @Test
    fun `reactive update from content to empty`() = runTest(testDispatcher) {
        val channelsFlow = MutableStateFlow(listOf(testChannel))
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { whitelistRepository.getChannelsByProfile("profile-1") } returns channelsFlow
        every { whitelistRepository.getVideosByProfile("profile-1") } returns flowOf(emptyList())
        every { whitelistRepository.getPlaylistsByProfile("profile-1") } returns flowOf(emptyList())
        setupDefaultTimeLimitChecker()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isEmpty).isFalse()

        channelsFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isEmpty).isTrue()
    }
}
