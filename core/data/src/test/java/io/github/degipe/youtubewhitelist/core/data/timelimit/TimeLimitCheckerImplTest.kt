package io.github.degipe.youtubewhitelist.core.data.timelimit

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.data.model.KidProfile
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.WatchHistoryRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TimeLimitCheckerImplTest {

    private lateinit var kidProfileRepository: KidProfileRepository
    private lateinit var watchHistoryRepository: WatchHistoryRepository
    private lateinit var checker: TimeLimitCheckerImpl

    private val testProfile = KidProfile(
        id = "profile-1", parentAccountId = "account-1",
        name = "Bence", avatarUrl = null,
        dailyLimitMinutes = 60, sleepPlaylistId = null,
        createdAt = 1000L
    )

    @Before
    fun setUp() {
        kidProfileRepository = mockk()
        watchHistoryRepository = mockk()
        checker = TimeLimitCheckerImpl(kidProfileRepository, watchHistoryRepository)
    }

    @Test
    fun `no limit set returns null remaining`() = runTest {
        val profileNoLimit = testProfile.copy(dailyLimitMinutes = null)
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(profileNoLimit)
        every { watchHistoryRepository.getTotalWatchedSecondsTodayFlow("profile-1") } returns flowOf(1800)

        val status = checker.getTimeLimitStatus("profile-1").first()

        assertThat(status.dailyLimitMinutes).isNull()
        assertThat(status.remainingSeconds).isNull()
        assertThat(status.isLimitReached).isFalse()
    }

    @Test
    fun `limit not reached returns correct remaining`() = runTest {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { watchHistoryRepository.getTotalWatchedSecondsTodayFlow("profile-1") } returns flowOf(1800) // 30 min watched

        val status = checker.getTimeLimitStatus("profile-1").first()

        assertThat(status.dailyLimitMinutes).isEqualTo(60)
        assertThat(status.watchedTodaySeconds).isEqualTo(1800)
        assertThat(status.remainingSeconds).isEqualTo(1800) // 30 min remaining
        assertThat(status.isLimitReached).isFalse()
    }

    @Test
    fun `limit reached returns isLimitReached true`() = runTest {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { watchHistoryRepository.getTotalWatchedSecondsTodayFlow("profile-1") } returns flowOf(3700) // > 60 min

        val status = checker.getTimeLimitStatus("profile-1").first()

        assertThat(status.isLimitReached).isTrue()
        assertThat(status.remainingSeconds).isEqualTo(0)
    }

    @Test
    fun `limit exactly reached`() = runTest {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { watchHistoryRepository.getTotalWatchedSecondsTodayFlow("profile-1") } returns flowOf(3600) // exactly 60 min

        val status = checker.getTimeLimitStatus("profile-1").first()

        assertThat(status.isLimitReached).isTrue()
        assertThat(status.remainingSeconds).isEqualTo(0)
    }

    @Test
    fun `reactive updates when watch time changes`() = runTest {
        val watchedFlow = MutableStateFlow(1800) // 30 min
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
        every { watchHistoryRepository.getTotalWatchedSecondsTodayFlow("profile-1") } returns watchedFlow

        val status1 = checker.getTimeLimitStatus("profile-1").first()
        assertThat(status1.remainingSeconds).isEqualTo(1800)

        watchedFlow.value = 3600 // 60 min
        val status2 = checker.getTimeLimitStatus("profile-1").first()
        assertThat(status2.isLimitReached).isTrue()
    }

    @Test
    fun `profile not found returns no limit`() = runTest {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(null)
        every { watchHistoryRepository.getTotalWatchedSecondsTodayFlow("profile-1") } returns flowOf(0)

        val status = checker.getTimeLimitStatus("profile-1").first()

        assertThat(status.dailyLimitMinutes).isNull()
        assertThat(status.remainingSeconds).isNull()
        assertThat(status.isLimitReached).isFalse()
    }

    @Test
    fun `combines profile and watch time flows`() = runTest {
        val profileFlow = MutableStateFlow<KidProfile?>(testProfile)
        every { kidProfileRepository.getProfileById("profile-1") } returns profileFlow
        every { watchHistoryRepository.getTotalWatchedSecondsTodayFlow("profile-1") } returns flowOf(1800)

        val status1 = checker.getTimeLimitStatus("profile-1").first()
        assertThat(status1.dailyLimitMinutes).isEqualTo(60)

        profileFlow.value = testProfile.copy(dailyLimitMinutes = 30)
        val status2 = checker.getTimeLimitStatus("profile-1").first()
        assertThat(status2.dailyLimitMinutes).isEqualTo(30)
        assertThat(status2.isLimitReached).isTrue() // 30 min watched, 30 min limit
    }

    @Test
    fun `zero limit means always reached`() = runTest {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(
            testProfile.copy(dailyLimitMinutes = 0)
        )
        every { watchHistoryRepository.getTotalWatchedSecondsTodayFlow("profile-1") } returns flowOf(0)

        val status = checker.getTimeLimitStatus("profile-1").first()

        assertThat(status.isLimitReached).isTrue()
        assertThat(status.remainingSeconds).isEqualTo(0)
    }
}
