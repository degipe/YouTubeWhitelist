package io.github.degipe.youtubewhitelist.core.data.repository.impl

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.database.dao.WatchHistoryDao
import io.github.degipe.youtubewhitelist.core.database.entity.WatchHistoryEntity
import io.mockk.coEvery
import io.mockk.coVerify
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
}
