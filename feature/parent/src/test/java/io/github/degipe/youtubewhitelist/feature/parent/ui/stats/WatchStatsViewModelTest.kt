package io.github.degipe.youtubewhitelist.feature.parent.ui.stats

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.data.model.DailyWatchStat
import io.github.degipe.youtubewhitelist.core.data.model.KidProfile
import io.github.degipe.youtubewhitelist.core.data.model.WatchStats
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.WatchHistoryRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchStatsViewModelTest {

    private lateinit var watchHistoryRepository: WatchHistoryRepository
    private lateinit var kidProfileRepository: KidProfileRepository
    private val testDispatcher = StandardTestDispatcher()

    private val testProfile = KidProfile(
        id = "profile-1", parentAccountId = "account-1",
        name = "Bence", avatarUrl = null,
        dailyLimitMinutes = null, sleepPlaylistId = null,
        createdAt = 1000L
    )

    private val weeklyStats = WatchStats(
        totalWatchedSeconds = 3600,
        videosWatchedCount = 5,
        dailyBreakdown = listOf(
            DailyWatchStat(dayTimestamp = 86400000L, totalSeconds = 1800),
            DailyWatchStat(dayTimestamp = 172800000L, totalSeconds = 1800)
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        watchHistoryRepository = mockk()
        kidProfileRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupDefaults() {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        coEvery { watchHistoryRepository.getWatchStats("profile-1", any()) } returns weeklyStats
    }

    private fun createViewModel(profileId: String = "profile-1"): WatchStatsViewModel {
        return WatchStatsViewModel(
            watchHistoryRepository = watchHistoryRepository,
            kidProfileRepository = kidProfileRepository,
            profileId = profileId
        )
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun `loads profile name`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.profileName).isEqualTo("Bence")
    }

    @Test
    fun `loads weekly stats by default`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.selectedPeriod).isEqualTo(StatsPeriod.WEEK)
        assertThat(viewModel.uiState.value.videosWatchedCount).isEqualTo(5)
    }

    @Test
    fun `total watch time formatted as hours and minutes`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.totalWatchTimeFormatted).isEqualTo("1h 0m")
    }

    @Test
    fun `total watch time formatted minutes only when less than hour`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        coEvery { watchHistoryRepository.getWatchStats("profile-1", any()) } returns weeklyStats.copy(
            totalWatchedSeconds = 1500
        )
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.totalWatchTimeFormatted).isEqualTo("25m")
    }

    @Test
    fun `daily breakdown maps to DailyStatItem`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.dailyBreakdown).hasSize(2)
        assertThat(viewModel.uiState.value.dailyBreakdown[0].minutes).isEqualTo(30)
    }

    @Test
    fun `selectPeriod DAY updates stats`() = runTest(testDispatcher) {
        setupDefaults()
        val dailyStats = WatchStats(
            totalWatchedSeconds = 600,
            videosWatchedCount = 2,
            dailyBreakdown = listOf(DailyWatchStat(dayTimestamp = 86400000L, totalSeconds = 600))
        )
        coEvery { watchHistoryRepository.getWatchStats("profile-1", any()) } returns dailyStats
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectPeriod(StatsPeriod.DAY)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedPeriod).isEqualTo(StatsPeriod.DAY)
        assertThat(viewModel.uiState.value.videosWatchedCount).isEqualTo(2)
    }

    @Test
    fun `selectPeriod MONTH updates stats`() = runTest(testDispatcher) {
        setupDefaults()
        val monthlyStats = WatchStats(
            totalWatchedSeconds = 36000,
            videosWatchedCount = 50,
            dailyBreakdown = emptyList()
        )
        coEvery { watchHistoryRepository.getWatchStats("profile-1", any()) } returns monthlyStats
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectPeriod(StatsPeriod.MONTH)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedPeriod).isEqualTo(StatsPeriod.MONTH)
        assertThat(viewModel.uiState.value.videosWatchedCount).isEqualTo(50)
    }

    @Test
    fun `empty stats shows zeros`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        coEvery { watchHistoryRepository.getWatchStats("profile-1", any()) } returns WatchStats(
            totalWatchedSeconds = 0, videosWatchedCount = 0, dailyBreakdown = emptyList()
        )
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.totalWatchTimeFormatted).isEqualTo("0m")
        assertThat(viewModel.uiState.value.videosWatchedCount).isEqualTo(0)
        assertThat(viewModel.uiState.value.dailyBreakdown).isEmpty()
    }

    @Test
    fun `null profile shows empty name`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(null)
        coEvery { watchHistoryRepository.getWatchStats("profile-1", any()) } returns weeklyStats
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.profileName).isEmpty()
    }

    @Test
    fun `isLoading false after stats loaded`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }
}
