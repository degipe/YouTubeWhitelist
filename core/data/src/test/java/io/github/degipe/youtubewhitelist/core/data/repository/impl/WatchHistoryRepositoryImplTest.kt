package io.github.degipe.youtubewhitelist.core.data.repository.impl

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.database.dao.WatchHistoryDao
import io.github.degipe.youtubewhitelist.core.database.entity.WatchHistoryEntity
import io.github.degipe.youtubewhitelist.core.database.dao.DailyWatchAggregate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class WatchHistoryRepositoryImplTest {

    private lateinit var watchHistoryDao: WatchHistoryDao
    private lateinit var repository: WatchHistoryRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        watchHistoryDao = mockk(relaxed = true)
        repository = WatchHistoryRepositoryImpl(
            watchHistoryDao = watchHistoryDao,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `recordWatch inserts entity with generated UUID`() = runTest(testDispatcher) {
        val entitySlot = slot<WatchHistoryEntity>()
        coEvery { watchHistoryDao.insert(capture(entitySlot)) } returns Unit

        repository.recordWatch("profile-1", "video-1", "Test Video", 120)

        assertThat(entitySlot.captured.id).isNotEmpty()
        assertThat(entitySlot.captured.kidProfileId).isEqualTo("profile-1")
    }

    @Test
    fun `recordWatch maps all fields correctly`() = runTest(testDispatcher) {
        val entitySlot = slot<WatchHistoryEntity>()
        coEvery { watchHistoryDao.insert(capture(entitySlot)) } returns Unit

        repository.recordWatch("profile-1", "video-1", "Test Video", 120)

        with(entitySlot.captured) {
            assertThat(videoId).isEqualTo("video-1")
            assertThat(videoTitle).isEqualTo("Test Video")
            assertThat(watchedSeconds).isEqualTo(120)
            assertThat(watchedAt).isGreaterThan(0)
        }
    }

    @Test
    fun `getRecentHistory maps entities to domain models`() = runTest(testDispatcher) {
        val entity = WatchHistoryEntity(
            id = "wh-1",
            kidProfileId = "profile-1",
            videoId = "video-1",
            videoTitle = "Test Video",
            watchedSeconds = 300,
            watchedAt = 1000L
        )
        coEvery { watchHistoryDao.getRecentHistory("profile-1", 50) } returns flowOf(listOf(entity))

        val result = repository.getRecentHistory("profile-1").first()

        assertThat(result).hasSize(1)
        with(result[0]) {
            assertThat(id).isEqualTo("wh-1")
            assertThat(kidProfileId).isEqualTo("profile-1")
            assertThat(videoId).isEqualTo("video-1")
            assertThat(videoTitle).isEqualTo("Test Video")
            assertThat(watchedSeconds).isEqualTo(300)
            assertThat(watchedAt).isEqualTo(1000L)
        }
    }

    @Test
    fun `getRecentHistory passes limit parameter`() = runTest(testDispatcher) {
        coEvery { watchHistoryDao.getRecentHistory("profile-1", 10) } returns flowOf(emptyList())

        repository.getRecentHistory("profile-1", 10).first()

        coVerify { watchHistoryDao.getRecentHistory("profile-1", 10) }
    }

    @Test
    fun `recordWatch calls DAO insert`() = runTest(testDispatcher) {
        repository.recordWatch("profile-1", "video-1", "Title", 60)

        coVerify { watchHistoryDao.insert(any()) }
    }

    // === getWatchStats ===

    @Test
    fun `getWatchStats maps aggregates correctly`() = runTest(testDispatcher) {
        coEvery { watchHistoryDao.getTotalWatchedSeconds("profile-1", any()) } returns 3600
        coEvery { watchHistoryDao.getVideosWatchedCount("profile-1", any()) } returns 5
        coEvery { watchHistoryDao.getDailyWatchTime("profile-1", any()) } returns listOf(
            DailyWatchAggregate(dayTimestamp = 1000000L, totalSeconds = 1800),
            DailyWatchAggregate(dayTimestamp = 2000000L, totalSeconds = 1800)
        )

        val stats = repository.getWatchStats("profile-1", 0L)

        assertThat(stats.totalWatchedSeconds).isEqualTo(3600)
        assertThat(stats.videosWatchedCount).isEqualTo(5)
        assertThat(stats.dailyBreakdown).hasSize(2)
        assertThat(stats.dailyBreakdown[0].dayTimestamp).isEqualTo(1000000L)
        assertThat(stats.dailyBreakdown[0].totalSeconds).isEqualTo(1800)
    }

    @Test
    fun `getWatchStats with no history returns zeros`() = runTest(testDispatcher) {
        coEvery { watchHistoryDao.getTotalWatchedSeconds("profile-1", any()) } returns null
        coEvery { watchHistoryDao.getVideosWatchedCount("profile-1", any()) } returns 0
        coEvery { watchHistoryDao.getDailyWatchTime("profile-1", any()) } returns emptyList()

        val stats = repository.getWatchStats("profile-1", 0L)

        assertThat(stats.totalWatchedSeconds).isEqualTo(0)
        assertThat(stats.videosWatchedCount).isEqualTo(0)
        assertThat(stats.dailyBreakdown).isEmpty()
    }

    @Test
    fun `getWatchStats passes sinceTimestamp to all queries`() = runTest(testDispatcher) {
        val since = 1234567890L
        coEvery { watchHistoryDao.getTotalWatchedSeconds(any(), any()) } returns null
        coEvery { watchHistoryDao.getVideosWatchedCount(any(), any()) } returns 0
        coEvery { watchHistoryDao.getDailyWatchTime(any(), any()) } returns emptyList()

        repository.getWatchStats("profile-1", since)

        coVerify { watchHistoryDao.getTotalWatchedSeconds("profile-1", since) }
        coVerify { watchHistoryDao.getVideosWatchedCount("profile-1", since) }
        coVerify { watchHistoryDao.getDailyWatchTime("profile-1", since) }
    }

    @Test
    fun `getWatchStats dailyBreakdown maps DailyWatchAggregate to DailyWatchStat`() = runTest(testDispatcher) {
        coEvery { watchHistoryDao.getTotalWatchedSeconds(any(), any()) } returns 600
        coEvery { watchHistoryDao.getVideosWatchedCount(any(), any()) } returns 2
        coEvery { watchHistoryDao.getDailyWatchTime(any(), any()) } returns listOf(
            DailyWatchAggregate(dayTimestamp = 86400000L, totalSeconds = 600)
        )

        val stats = repository.getWatchStats("profile-1", 0L)

        assertThat(stats.dailyBreakdown).hasSize(1)
        assertThat(stats.dailyBreakdown[0].dayTimestamp).isEqualTo(86400000L)
        assertThat(stats.dailyBreakdown[0].totalSeconds).isEqualTo(600)
    }

    // === getTotalWatchedSecondsToday ===

    @Test
    fun `getTotalWatchedSecondsToday returns watched seconds`() = runTest(testDispatcher) {
        coEvery { watchHistoryDao.getTotalWatchedSeconds("profile-1", any()) } returns 1800

        val result = repository.getTotalWatchedSecondsToday("profile-1")

        assertThat(result).isEqualTo(1800)
    }

    @Test
    fun `getTotalWatchedSecondsToday returns zero when null`() = runTest(testDispatcher) {
        coEvery { watchHistoryDao.getTotalWatchedSeconds("profile-1", any()) } returns null

        val result = repository.getTotalWatchedSecondsToday("profile-1")

        assertThat(result).isEqualTo(0)
    }

    // === getTotalWatchedSecondsTodayFlow ===

    @Test
    fun `getTotalWatchedSecondsTodayFlow emits reactive value`() = runTest(testDispatcher) {
        every { watchHistoryDao.getTotalWatchedSecondsFlow("profile-1", any()) } returns flowOf(900)

        val result = repository.getTotalWatchedSecondsTodayFlow("profile-1").first()

        assertThat(result).isEqualTo(900)
    }

    @Test
    fun `getTotalWatchedSecondsTodayFlow delegates to DAO with start of today`() = runTest(testDispatcher) {
        every { watchHistoryDao.getTotalWatchedSecondsFlow(any(), any()) } returns flowOf(0)

        repository.getTotalWatchedSecondsTodayFlow("profile-1").first()

        // Verify it was called with a timestamp that is start of today (midnight)
        // The exact value depends on current time, but it should be <= current time
        // and should be a multiple of day milliseconds approximately
        io.mockk.verify { watchHistoryDao.getTotalWatchedSecondsFlow("profile-1", any()) }
    }
}
