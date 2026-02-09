package io.github.degipe.youtubewhitelist.core.export

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.database.dao.KidProfileDao
import io.github.degipe.youtubewhitelist.core.database.dao.WhitelistItemDao
import io.github.degipe.youtubewhitelist.core.database.entity.KidProfileEntity
import io.github.degipe.youtubewhitelist.core.database.entity.WhitelistItemEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test

class ExportImportServiceImplTest {

    private lateinit var kidProfileDao: KidProfileDao
    private lateinit var whitelistItemDao: WhitelistItemDao
    private lateinit var service: ExportImportServiceImpl

    private val parentId = "parent-1"

    private val profile1 = KidProfileEntity(
        id = "profile-1", parentAccountId = parentId,
        name = "Bence", avatarUrl = "https://avatar1.jpg",
        dailyLimitMinutes = 60, sleepPlaylistId = "PL123",
        createdAt = 1000L
    )

    private val profile2 = KidProfileEntity(
        id = "profile-2", parentAccountId = parentId,
        name = "Emma", avatarUrl = null,
        dailyLimitMinutes = null, sleepPlaylistId = null,
        createdAt = 2000L
    )

    private val item1 = WhitelistItemEntity(
        id = "item-1", kidProfileId = "profile-1",
        type = WhitelistItemType.CHANNEL,
        youtubeId = "UC123", title = "Channel One",
        thumbnailUrl = "https://thumb1.jpg", channelTitle = null,
        addedAt = 3000L
    )

    private val item2 = WhitelistItemEntity(
        id = "item-2", kidProfileId = "profile-1",
        type = WhitelistItemType.VIDEO,
        youtubeId = "vid456", title = "Cool Video",
        thumbnailUrl = "https://thumb2.jpg", channelTitle = "Channel One",
        addedAt = 4000L
    )

    @Before
    fun setUp() {
        kidProfileDao = mockk(relaxed = true)
        whitelistItemDao = mockk(relaxed = true)
        service = ExportImportServiceImpl(kidProfileDao, whitelistItemDao)
    }

    // ===== EXPORT TESTS =====

    @Test
    fun `export returns valid json with profiles and items`() = runTest {
        coEvery { kidProfileDao.getProfilesByParent(parentId) } returns flowOf(listOf(profile1))
        coEvery { whitelistItemDao.getItemsByProfile("profile-1") } returns flowOf(listOf(item1, item2))

        val result = service.exportToJson(parentId)

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val json = (result as AppResult.Success).data
        assertThat(json).contains("Bence")
        assertThat(json).contains("UC123")
        assertThat(json).contains("vid456")
    }

    @Test
    fun `export includes all profile fields`() = runTest {
        coEvery { kidProfileDao.getProfilesByParent(parentId) } returns flowOf(listOf(profile1))
        coEvery { whitelistItemDao.getItemsByProfile("profile-1") } returns flowOf(listOf())

        val result = service.exportToJson(parentId)
        val json = (result as AppResult.Success).data
        val exportData = Json.decodeFromString<io.github.degipe.youtubewhitelist.core.export.model.ExportData>(json)

        with(exportData.profiles[0]) {
            assertThat(name).isEqualTo("Bence")
            assertThat(avatarUrl).isEqualTo("https://avatar1.jpg")
            assertThat(dailyLimitMinutes).isEqualTo(60)
            assertThat(sleepPlaylistId).isEqualTo("PL123")
        }
    }

    @Test
    fun `export includes whitelist item fields`() = runTest {
        coEvery { kidProfileDao.getProfilesByParent(parentId) } returns flowOf(listOf(profile1))
        coEvery { whitelistItemDao.getItemsByProfile("profile-1") } returns flowOf(listOf(item1))

        val result = service.exportToJson(parentId)
        val json = (result as AppResult.Success).data
        val exportData = Json.decodeFromString<io.github.degipe.youtubewhitelist.core.export.model.ExportData>(json)

        with(exportData.profiles[0].whitelistItems[0]) {
            assertThat(type).isEqualTo("CHANNEL")
            assertThat(youtubeId).isEqualTo("UC123")
            assertThat(title).isEqualTo("Channel One")
            assertThat(thumbnailUrl).isEqualTo("https://thumb1.jpg")
        }
    }

    @Test
    fun `export multiple profiles`() = runTest {
        coEvery { kidProfileDao.getProfilesByParent(parentId) } returns flowOf(listOf(profile1, profile2))
        coEvery { whitelistItemDao.getItemsByProfile("profile-1") } returns flowOf(listOf(item1))
        coEvery { whitelistItemDao.getItemsByProfile("profile-2") } returns flowOf(listOf())

        val result = service.exportToJson(parentId)
        val json = (result as AppResult.Success).data
        val exportData = Json.decodeFromString<io.github.degipe.youtubewhitelist.core.export.model.ExportData>(json)

        assertThat(exportData.profiles).hasSize(2)
        assertThat(exportData.profiles[0].name).isEqualTo("Bence")
        assertThat(exportData.profiles[1].name).isEqualTo("Emma")
    }

    @Test
    fun `export empty profiles returns empty list`() = runTest {
        coEvery { kidProfileDao.getProfilesByParent(parentId) } returns flowOf(emptyList())

        val result = service.exportToJson(parentId)
        val json = (result as AppResult.Success).data
        val exportData = Json.decodeFromString<io.github.degipe.youtubewhitelist.core.export.model.ExportData>(json)

        assertThat(exportData.profiles).isEmpty()
    }

    @Test
    fun `export sets version and exportedAt`() = runTest {
        coEvery { kidProfileDao.getProfilesByParent(parentId) } returns flowOf(emptyList())

        val result = service.exportToJson(parentId)
        val json = (result as AppResult.Success).data
        val exportData = Json.decodeFromString<io.github.degipe.youtubewhitelist.core.export.model.ExportData>(json)

        assertThat(exportData.version).isEqualTo(1)
        assertThat(exportData.exportedAt).isGreaterThan(0)
    }

    // ===== IMPORT MERGE TESTS =====

    @Test
    fun `import merge creates new profiles with new IDs`() = runTest {
        val json = createExportJson(
            listOf(
                createExportProfile("Bence", items = listOf(
                    createExportItem("CHANNEL", "UC123", "Channel One")
                ))
            )
        )

        val profileSlot = slot<KidProfileEntity>()
        coEvery { kidProfileDao.insert(capture(profileSlot)) } returns Unit

        coEvery { whitelistItemDao.findByYoutubeId(any(), any()) } returns null
        val itemSlot = mutableListOf<WhitelistItemEntity>()
        coEvery { whitelistItemDao.insert(capture(itemSlot)) } returns Unit

        val result = service.importFromJson(parentId, json, ImportStrategy.MERGE)

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val importResult = (result as AppResult.Success).data
        assertThat(importResult.profilesImported).isEqualTo(1)
        assertThat(importResult.itemsImported).isEqualTo(1)

        // New UUID generated (not from export)
        assertThat(profileSlot.captured.id).isNotEmpty()
        assertThat(profileSlot.captured.parentAccountId).isEqualTo(parentId)
        assertThat(profileSlot.captured.name).isEqualTo("Bence")
    }

    @Test
    fun `import merge skips duplicate items by youtubeId`() = runTest {
        val json = createExportJson(
            listOf(
                createExportProfile("Bence", items = listOf(
                    createExportItem("CHANNEL", "UC123", "Channel One"),
                    createExportItem("VIDEO", "vid456", "Cool Video")
                ))
            )
        )

        val profileSlot = slot<KidProfileEntity>()
        coEvery { kidProfileDao.insert(capture(profileSlot)) } returns Unit

        // First item already exists, second doesn't
        coEvery { whitelistItemDao.findByYoutubeId(any(), "UC123") } returns item1
        coEvery { whitelistItemDao.findByYoutubeId(any(), "vid456") } returns null
        coEvery { whitelistItemDao.insert(any()) } returns Unit

        val result = service.importFromJson(parentId, json, ImportStrategy.MERGE)

        val importResult = (result as AppResult.Success).data
        assertThat(importResult.itemsImported).isEqualTo(1)
        assertThat(importResult.itemsSkipped).isEqualTo(1)
    }

    @Test
    fun `import merge multiple profiles`() = runTest {
        val json = createExportJson(
            listOf(
                createExportProfile("Bence", items = listOf()),
                createExportProfile("Emma", items = listOf())
            )
        )

        coEvery { kidProfileDao.insert(any()) } returns Unit

        val result = service.importFromJson(parentId, json, ImportStrategy.MERGE)

        val importResult = (result as AppResult.Success).data
        assertThat(importResult.profilesImported).isEqualTo(2)

        coVerify(exactly = 2) { kidProfileDao.insert(any()) }
    }

    // ===== IMPORT OVERWRITE TESTS =====

    @Test
    fun `import overwrite deletes existing profiles first`() = runTest {
        val json = createExportJson(
            listOf(
                createExportProfile("Bence", items = listOf())
            )
        )

        coEvery { kidProfileDao.getProfilesByParent(parentId) } returns flowOf(listOf(profile1, profile2))
        coEvery { kidProfileDao.delete(any()) } returns Unit
        coEvery { kidProfileDao.insert(any()) } returns Unit

        val result = service.importFromJson(parentId, json, ImportStrategy.OVERWRITE)

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        coVerify { kidProfileDao.delete(profile1) }
        coVerify { kidProfileDao.delete(profile2) }
    }

    @Test
    fun `import overwrite does not skip duplicates`() = runTest {
        val json = createExportJson(
            listOf(
                createExportProfile("Bence", items = listOf(
                    createExportItem("CHANNEL", "UC123", "Channel One")
                ))
            )
        )

        coEvery { kidProfileDao.getProfilesByParent(parentId) } returns flowOf(emptyList())
        coEvery { kidProfileDao.insert(any()) } returns Unit
        coEvery { whitelistItemDao.insert(any()) } returns Unit

        val result = service.importFromJson(parentId, json, ImportStrategy.OVERWRITE)

        val importResult = (result as AppResult.Success).data
        assertThat(importResult.itemsImported).isEqualTo(1)
        assertThat(importResult.itemsSkipped).isEqualTo(0)

        // No duplicate check for overwrite
        coVerify(exactly = 0) { whitelistItemDao.findByYoutubeId(any(), any()) }
    }

    // ===== ERROR TESTS =====

    @Test
    fun `import invalid json returns error`() = runTest {
        val result = service.importFromJson(parentId, "not valid json", ImportStrategy.MERGE)

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
    }

    @Test
    fun `import preserves profile optional fields`() = runTest {
        val json = createExportJson(
            listOf(
                createExportProfile(
                    "Bence",
                    avatarUrl = "https://avatar.jpg",
                    dailyLimitMinutes = 90,
                    sleepPlaylistId = "PLsleep",
                    items = listOf()
                )
            )
        )

        val profileSlot = slot<KidProfileEntity>()
        coEvery { kidProfileDao.insert(capture(profileSlot)) } returns Unit

        service.importFromJson(parentId, json, ImportStrategy.MERGE)

        with(profileSlot.captured) {
            assertThat(avatarUrl).isEqualTo("https://avatar.jpg")
            assertThat(dailyLimitMinutes).isEqualTo(90)
            assertThat(sleepPlaylistId).isEqualTo("PLsleep")
        }
    }

    @Test
    fun `import item maps type correctly`() = runTest {
        val json = createExportJson(
            listOf(
                createExportProfile("Bence", items = listOf(
                    createExportItem("PLAYLIST", "PL789", "My Playlist")
                ))
            )
        )

        coEvery { kidProfileDao.insert(any()) } returns Unit
        coEvery { whitelistItemDao.findByYoutubeId(any(), any()) } returns null
        val itemSlot = slot<WhitelistItemEntity>()
        coEvery { whitelistItemDao.insert(capture(itemSlot)) } returns Unit

        service.importFromJson(parentId, json, ImportStrategy.MERGE)

        assertThat(itemSlot.captured.type).isEqualTo(WhitelistItemType.PLAYLIST)
    }

    // ===== HELPERS =====

    private fun createExportJson(
        profiles: List<io.github.degipe.youtubewhitelist.core.export.model.ExportProfile>
    ): String {
        val exportData = io.github.degipe.youtubewhitelist.core.export.model.ExportData(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            profiles = profiles
        )
        return Json.encodeToString(io.github.degipe.youtubewhitelist.core.export.model.ExportData.serializer(), exportData)
    }

    private fun createExportProfile(
        name: String,
        avatarUrl: String? = null,
        dailyLimitMinutes: Int? = null,
        sleepPlaylistId: String? = null,
        items: List<io.github.degipe.youtubewhitelist.core.export.model.ExportWhitelistItem>
    ) = io.github.degipe.youtubewhitelist.core.export.model.ExportProfile(
        name = name,
        avatarUrl = avatarUrl,
        dailyLimitMinutes = dailyLimitMinutes,
        sleepPlaylistId = sleepPlaylistId,
        whitelistItems = items
    )

    private fun createExportItem(
        type: String,
        youtubeId: String,
        title: String,
        thumbnailUrl: String = "https://thumb.jpg",
        channelTitle: String? = null
    ) = io.github.degipe.youtubewhitelist.core.export.model.ExportWhitelistItem(
        type = type,
        youtubeId = youtubeId,
        title = title,
        thumbnailUrl = thumbnailUrl,
        channelTitle = channelTitle
    )
}
