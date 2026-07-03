package io.github.degipe.youtubewhitelist.core.export

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.database.YouTubeWhitelistDatabase
import io.github.degipe.youtubewhitelist.core.database.dao.KidProfileDao
import io.github.degipe.youtubewhitelist.core.database.dao.WhitelistItemDao
import io.github.degipe.youtubewhitelist.core.database.entity.KidProfileEntity
import io.github.degipe.youtubewhitelist.core.database.entity.WhitelistItemEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test

class ExportImportServiceImplTest {

    private lateinit var kidProfileDao: KidProfileDao
    private lateinit var whitelistItemDao: WhitelistItemDao
    private lateinit var database: YouTubeWhitelistDatabase
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
        database = mockk(relaxed = true)
        service = ExportImportServiceImpl(kidProfileDao, whitelistItemDao, database)
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

    // ===== IMPORT ERROR TESTS =====
    //
    // NOTE: All other import behavior (MERGE dedup-by-name, OVERWRITE delete-then-insert,
    // duplicate item skipping, optional field / type mapping, and transactional rollback) is
    // now covered by ExportImportRoundTripTest, which exercises a real in-memory Room database.
    // Those cases used to live here as mockk-based tests, but mocking `KidProfileDao` /
    // `WhitelistItemDao` in isolation could not exercise the real dedup query or the
    // `database.withTransaction` wrapper added in this fix - worse, the old mocks (e.g.
    // `findByYoutubeId(any(), "UC123")` matching regardless of profile id) masked the very bug
    // being fixed here (MERGE always minted a fresh, empty profile id, so the dedup query could
    // never find a real match). Import tests belong against a real DB from here on.

    @Test
    fun `import invalid json returns error`() = runTest {
        val result = service.importFromJson(parentId, "not valid json", ImportStrategy.MERGE)

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
    }
}
