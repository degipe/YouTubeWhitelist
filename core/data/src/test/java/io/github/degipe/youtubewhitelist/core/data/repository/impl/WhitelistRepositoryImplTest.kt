package io.github.degipe.youtubewhitelist.core.data.repository.impl

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.model.YouTubeMetadata
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import io.github.degipe.youtubewhitelist.core.database.dao.WhitelistItemDao
import io.github.degipe.youtubewhitelist.core.database.entity.WhitelistItemEntity
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

class WhitelistRepositoryImplTest {

    private lateinit var whitelistItemDao: WhitelistItemDao
    private lateinit var youTubeApiRepository: YouTubeApiRepository
    private lateinit var repository: WhitelistRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        whitelistItemDao = mockk(relaxed = true)
        youTubeApiRepository = mockk()
        repository = WhitelistRepositoryImpl(whitelistItemDao, youTubeApiRepository, testDispatcher)
    }

    // === getItemsByProfile ===

    @Test
    fun `getItemsByProfile maps entities to domain models`() = runTest(testDispatcher) {
        val entities = listOf(
            WhitelistItemEntity(
                id = "item1",
                kidProfileId = "profile1",
                type = WhitelistItemType.VIDEO,
                youtubeId = "vid123",
                title = "Test Video",
                thumbnailUrl = "https://img/thumb.jpg",
                channelTitle = "Test Channel",
                addedAt = 1000L
            )
        )
        every { whitelistItemDao.getItemsByProfile("profile1") } returns flowOf(entities)

        val items = repository.getItemsByProfile("profile1").first()

        assertThat(items).hasSize(1)
        assertThat(items[0].id).isEqualTo("item1")
        assertThat(items[0].type).isEqualTo(WhitelistItemType.VIDEO)
        assertThat(items[0].youtubeId).isEqualTo("vid123")
        assertThat(items[0].title).isEqualTo("Test Video")
        assertThat(items[0].channelTitle).isEqualTo("Test Channel")
    }

    // === addItemFromUrl - Video ===

    @Test
    fun `addItemFromUrl parses video url and fetches metadata`() = runTest(testDispatcher) {
        coEvery { whitelistItemDao.findByYoutubeId("profile1", "dQw4w9WgXcQ") } returns null
        coEvery { youTubeApiRepository.getVideoById("dQw4w9WgXcQ") } returns AppResult.Success(
            YouTubeMetadata.Video(
                youtubeId = "dQw4w9WgXcQ",
                title = "Rick Astley",
                thumbnailUrl = "https://img/thumb.jpg",
                channelId = "UC123",
                channelTitle = "Rick Astley",
                description = "Never gonna give you up",
                duration = "PT3M33S"
            )
        )

        val result = repository.addItemFromUrl("profile1", "https://www.youtube.com/watch?v=dQw4w9WgXcQ")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val item = (result as AppResult.Success).data
        assertThat(item.type).isEqualTo(WhitelistItemType.VIDEO)
        assertThat(item.youtubeId).isEqualTo("dQw4w9WgXcQ")
        assertThat(item.title).isEqualTo("Rick Astley")
        assertThat(item.channelTitle).isEqualTo("Rick Astley")

        val entitySlot = slot<WhitelistItemEntity>()
        coVerify { whitelistItemDao.insert(capture(entitySlot)) }
        assertThat(entitySlot.captured.youtubeId).isEqualTo("dQw4w9WgXcQ")
        assertThat(entitySlot.captured.type).isEqualTo(WhitelistItemType.VIDEO)
    }

    // === addItemFromUrl - Channel ===

    @Test
    fun `addItemFromUrl parses channel url and fetches metadata`() = runTest(testDispatcher) {
        coEvery { whitelistItemDao.findByYoutubeId("profile1", "UC123") } returns null
        coEvery { youTubeApiRepository.getChannelById("UC123") } returns AppResult.Success(
            YouTubeMetadata.Channel(
                youtubeId = "UC123",
                title = "Test Channel",
                thumbnailUrl = "https://img/thumb.jpg",
                description = "A test channel",
                subscriberCount = "50000",
                videoCount = "100",
                uploadsPlaylistId = null
            )
        )

        val result = repository.addItemFromUrl("profile1", "https://www.youtube.com/channel/UC123")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val item = (result as AppResult.Success).data
        assertThat(item.type).isEqualTo(WhitelistItemType.CHANNEL)
        assertThat(item.youtubeId).isEqualTo("UC123")
    }

    // === addItemFromUrl - Channel handle ===

    @Test
    fun `addItemFromUrl resolves channel handle via API`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelByHandle("MrBeast") } returns AppResult.Success(
            YouTubeMetadata.Channel(
                youtubeId = "UC_resolved",
                title = "MrBeast",
                thumbnailUrl = "https://img/thumb.jpg",
                description = "MrBeast channel",
                subscriberCount = null,
                videoCount = null,
                uploadsPlaylistId = null
            )
        )
        coEvery { whitelistItemDao.findByYoutubeId("profile1", "UC_resolved") } returns null

        val result = repository.addItemFromUrl("profile1", "https://www.youtube.com/@MrBeast")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val item = (result as AppResult.Success).data
        assertThat(item.type).isEqualTo(WhitelistItemType.CHANNEL)
        assertThat(item.youtubeId).isEqualTo("UC_resolved")
        assertThat(item.title).isEqualTo("MrBeast")
    }

    // === addItemFromUrl - Playlist ===

    @Test
    fun `addItemFromUrl parses playlist url and fetches metadata`() = runTest(testDispatcher) {
        coEvery { whitelistItemDao.findByYoutubeId("profile1", "PL123") } returns null
        coEvery { youTubeApiRepository.getPlaylistById("PL123") } returns AppResult.Success(
            YouTubeMetadata.Playlist(
                youtubeId = "PL123",
                title = "Test Playlist",
                thumbnailUrl = "https://img/thumb.jpg",
                channelId = "UC123",
                channelTitle = "Test Channel",
                description = "A playlist"
            )
        )

        val result = repository.addItemFromUrl("profile1", "https://www.youtube.com/playlist?list=PL123")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val item = (result as AppResult.Success).data
        assertThat(item.type).isEqualTo(WhitelistItemType.PLAYLIST)
        assertThat(item.youtubeId).isEqualTo("PL123")
    }

    // === addItemFromUrl - Channel custom ===

    @Test
    fun `addItemFromUrl resolves channel custom url via API`() = runTest(testDispatcher) {
        coEvery { youTubeApiRepository.getChannelByHandle("PewDiePie") } returns AppResult.Success(
            YouTubeMetadata.Channel(
                youtubeId = "UC_pewdiepie",
                title = "PewDiePie",
                thumbnailUrl = "https://img/thumb.jpg",
                description = "PewDiePie channel",
                subscriberCount = null,
                videoCount = null,
                uploadsPlaylistId = null
            )
        )
        coEvery { whitelistItemDao.findByYoutubeId("profile1", "UC_pewdiepie") } returns null

        val result = repository.addItemFromUrl("profile1", "https://www.youtube.com/c/PewDiePie")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val item = (result as AppResult.Success).data
        assertThat(item.type).isEqualTo(WhitelistItemType.CHANNEL)
        assertThat(item.youtubeId).isEqualTo("UC_pewdiepie")
    }

    // === addItemFromUrl - early duplicate check ===

    @Test
    fun `addItemFromUrl returns error for duplicate video without API call`() = runTest(testDispatcher) {
        coEvery { whitelistItemDao.findByYoutubeId("profile1", "dQw4w9WgXcQ") } returns
            WhitelistItemEntity(
                id = "existing",
                kidProfileId = "profile1",
                type = WhitelistItemType.VIDEO,
                youtubeId = "dQw4w9WgXcQ",
                title = "Existing",
                thumbnailUrl = "https://img/thumb.jpg"
            )

        val result = repository.addItemFromUrl("profile1", "https://www.youtube.com/watch?v=dQw4w9WgXcQ")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        // Verify that no API call was made (early duplicate check)
        coVerify(exactly = 0) { youTubeApiRepository.getVideoById(any()) }
    }

    // === Error cases ===

    @Test
    fun `addItemFromUrl returns error for invalid url`() = runTest(testDispatcher) {
        val result = repository.addItemFromUrl("profile1", "https://www.google.com")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).message).contains("Invalid YouTube URL")
    }

    @Test
    fun `addItemFromUrl returns error when API fails`() = runTest(testDispatcher) {
        coEvery { whitelistItemDao.findByYoutubeId("profile1", "vid123") } returns null
        coEvery { youTubeApiRepository.getVideoById("vid123") } returns
            AppResult.Error("Network error")

        val result = repository.addItemFromUrl("profile1", "https://www.youtube.com/watch?v=vid123")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
    }

    // === removeItem ===

    @Test
    fun `removeItem delegates to dao`() = runTest(testDispatcher) {
        val item = io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem(
            id = "item1",
            kidProfileId = "profile1",
            type = WhitelistItemType.VIDEO,
            youtubeId = "vid123",
            title = "Test",
            thumbnailUrl = "https://img/thumb.jpg",
            channelTitle = null,
            addedAt = 1000L
        )

        repository.removeItem(item)

        coVerify { whitelistItemDao.delete(any()) }
    }

    // === isAlreadyWhitelisted ===

    @Test
    fun `isAlreadyWhitelisted returns true when item exists`() = runTest(testDispatcher) {
        coEvery { whitelistItemDao.findByYoutubeId("profile1", "vid123") } returns
            WhitelistItemEntity(
                id = "item1",
                kidProfileId = "profile1",
                type = WhitelistItemType.VIDEO,
                youtubeId = "vid123",
                title = "Test",
                thumbnailUrl = "https://img/thumb.jpg"
            )

        val result = repository.isAlreadyWhitelisted("profile1", "vid123")
        assertThat(result).isTrue()
    }

    @Test
    fun `isAlreadyWhitelisted returns false when item does not exist`() = runTest(testDispatcher) {
        coEvery { whitelistItemDao.findByYoutubeId("profile1", "vid123") } returns null

        val result = repository.isAlreadyWhitelisted("profile1", "vid123")
        assertThat(result).isFalse()
    }

    // === getItemCount ===

    @Test
    fun `getItemCount delegates to dao`() = runTest(testDispatcher) {
        coEvery { whitelistItemDao.getItemCount("profile1") } returns 5

        val count = repository.getItemCount("profile1")
        assertThat(count).isEqualTo(5)
    }
}
