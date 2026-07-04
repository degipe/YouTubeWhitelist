package io.github.degipe.youtubewhitelist.core.export

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.database.YouTubeWhitelistDatabase
import io.github.degipe.youtubewhitelist.core.database.entity.KidProfileEntity
import io.github.degipe.youtubewhitelist.core.database.entity.ParentAccountEntity
import io.github.degipe.youtubewhitelist.core.database.entity.WhitelistItemEntity
import io.github.degipe.youtubewhitelist.core.export.model.ExportData
import io.github.degipe.youtubewhitelist.core.export.model.ExportProfile
import io.github.degipe.youtubewhitelist.core.export.model.ExportWhitelistItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Real-DB (Robolectric, in-memory Room) regression tests for the two bugs fixed in
 * Task 1.2:
 *  1. MERGE minted a fresh profile UUID on every import, so the in-profile dedup query
 *     (`findByYoutubeId(newProfileId, ...)`) always ran against an empty profile and never
 *     found a match -> re-importing the same backup duplicated profiles and items.
 *  2. `importFromJson` was not transactional, so an OVERWRITE that deletes existing profiles
 *     and then throws mid-way (e.g. an unknown WhitelistItemType) destroyed user data with no
 *     rollback.
 *
 * Mockk-based unit tests can't exercise these bugs meaningfully because the fix relies on
 * real SQLite transaction semantics (`database.withTransaction`) and real dedup queries
 * against actual rows - hence a real in-memory Room database here instead of mocked DAOs.
 */
@RunWith(RobolectricTestRunner::class)
class ExportImportRoundTripTest {

    private lateinit var db: YouTubeWhitelistDatabase
    private lateinit var service: ExportImportServiceImpl

    private val json = Json { prettyPrint = true }

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, YouTubeWhitelistDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        service = ExportImportServiceImpl(db.kidProfileDao(), db.whitelistItemDao(), db)

        runBlocking {
            db.parentAccountDao().insert(
                ParentAccountEntity(
                    id = PARENT_ID,
                    googleAccountId = "google-1",
                    email = "parent@example.com",
                    pinHash = "hash"
                )
            )
        }
    }

    @After
    fun teardown() = db.close()

    @Test
    fun `re-import MERGE does not duplicate profiles or items`() = runTest {
        // Simulates a user importing the same backup file twice (e.g. double-tapping "restore").
        val backupJson = json.encodeToString(
            ExportData.serializer(),
            ExportData(
                version = 1,
                exportedAt = 1_000L,
                profiles = listOf(
                    ExportProfile(
                        name = "Kid A",
                        avatarUrl = null,
                        dailyLimitMinutes = 60,
                        sleepPlaylistId = null,
                        whitelistItems = listOf(
                            ExportWhitelistItem(
                                type = "CHANNEL",
                                youtubeId = "UC1234567890123456789012",
                                title = "Channel One",
                                thumbnailUrl = "https://thumb.jpg"
                            )
                        )
                    )
                )
            )
        )

        val first = service.importFromJson(PARENT_ID, backupJson, ImportStrategy.MERGE)
        val second = service.importFromJson(PARENT_ID, backupJson, ImportStrategy.MERGE)

        assertThat(first).isInstanceOf(AppResult.Success::class.java)
        assertThat(second).isInstanceOf(AppResult.Success::class.java)

        val profiles = db.kidProfileDao().getProfilesByParent(PARENT_ID).first()
        val kidAProfiles = profiles.filter { it.name == "Kid A" }
        // Idempotent MERGE: re-importing the exact same backup must add NO duplicates at all.
        assertThat(kidAProfiles).hasSize(1)

        val items = db.whitelistItemDao().getItemsByProfile(kidAProfiles.single().id).first()
        assertThat(items).hasSize(1)

        // The second import must have reused the profile created by the first import and
        // skipped the duplicate item.
        val secondResult = (second as AppResult.Success).data
        assertThat(secondResult.profilesImported).isEqualTo(0)
        assertThat(secondResult.itemsImported).isEqualTo(0)
        assertThat(secondResult.itemsSkipped).isEqualTo(1)
    }

    @Test
    fun `MERGE creates a separate profile for each new profile name`() = runTest {
        val backupJson = json.encodeToString(
            ExportData.serializer(),
            ExportData(
                version = 1,
                exportedAt = 1_000L,
                profiles = listOf(
                    ExportProfile(name = "Bence", whitelistItems = emptyList()),
                    ExportProfile(name = "Emma", whitelistItems = emptyList())
                )
            )
        )

        val result = service.importFromJson(PARENT_ID, backupJson, ImportStrategy.MERGE)

        val importResult = (result as AppResult.Success).data
        assertThat(importResult.profilesImported).isEqualTo(2)

        val profiles = db.kidProfileDao().getProfilesByParent(PARENT_ID).first()
        assertThat(profiles.map { it.name }).containsExactly("Bence", "Emma")
    }

    @Test
    fun `MERGE preserves profile optional fields and maps whitelist item type`() = runTest {
        val backupJson = json.encodeToString(
            ExportData.serializer(),
            ExportData(
                version = 1,
                exportedAt = 1_000L,
                profiles = listOf(
                    ExportProfile(
                        name = "Bence",
                        avatarUrl = "https://avatar.jpg",
                        dailyLimitMinutes = 90,
                        sleepPlaylistId = "PLsleep",
                        whitelistItems = listOf(
                            ExportWhitelistItem(
                                type = "PLAYLIST",
                                youtubeId = "PL1234567890",
                                title = "My Playlist",
                                thumbnailUrl = "https://thumb.jpg"
                            )
                        )
                    )
                )
            )
        )

        service.importFromJson(PARENT_ID, backupJson, ImportStrategy.MERGE)

        val profile = db.kidProfileDao().getProfilesByParent(PARENT_ID).first().single()
        assertThat(profile.avatarUrl).isEqualTo("https://avatar.jpg")
        assertThat(profile.dailyLimitMinutes).isEqualTo(90)
        assertThat(profile.sleepPlaylistId).isEqualTo("PLsleep")

        val item = db.whitelistItemDao().getItemsByProfile(profile.id).first().single()
        assertThat(item.type).isEqualTo(WhitelistItemType.PLAYLIST)
    }

    @Test
    fun `OVERWRITE deletes existing profiles first and does not dedupe items`() = runTest {
        val existingProfileId = "existing-profile"
        runBlocking {
            db.kidProfileDao().insert(
                KidProfileEntity(
                    id = existingProfileId,
                    parentAccountId = PARENT_ID,
                    name = "Old Kid",
                    avatarUrl = null,
                    dailyLimitMinutes = null,
                    sleepPlaylistId = null
                )
            )
        }

        // Same profile name AND same youtubeId as an item that would exist post-delete - OVERWRITE
        // must not consult findByYoutubeId at all, since the old profile is gone.
        val backupJson = json.encodeToString(
            ExportData.serializer(),
            ExportData(
                version = 1,
                exportedAt = 1_000L,
                profiles = listOf(
                    ExportProfile(
                        name = "New Kid",
                        whitelistItems = listOf(
                            ExportWhitelistItem(
                                type = "CHANNEL",
                                youtubeId = "UC1234567890123456789012",
                                title = "Channel One",
                                thumbnailUrl = "https://thumb.jpg"
                            )
                        )
                    )
                )
            )
        )

        val result = service.importFromJson(PARENT_ID, backupJson, ImportStrategy.OVERWRITE)

        val importResult = (result as AppResult.Success).data
        assertThat(importResult.profilesImported).isEqualTo(1)
        assertThat(importResult.itemsImported).isEqualTo(1)
        assertThat(importResult.itemsSkipped).isEqualTo(0)

        val profiles = db.kidProfileDao().getProfilesByParent(PARENT_ID).first()
        assertThat(profiles.map { it.name }).containsExactly("New Kid")
        assertThat(profiles.none { it.id == existingProfileId }).isTrue()
    }

    @Test
    fun `OVERWRITE with invalid item type rolls back and keeps original data`() = runTest {
        val existingProfileId = "existing-profile"
        runBlocking {
            db.kidProfileDao().insert(
                KidProfileEntity(
                    id = existingProfileId,
                    parentAccountId = PARENT_ID,
                    name = "Existing Kid",
                    avatarUrl = null,
                    dailyLimitMinutes = null,
                    sleepPlaylistId = null
                )
            )
            db.whitelistItemDao().insert(
                WhitelistItemEntity(
                    id = "existing-item",
                    kidProfileId = existingProfileId,
                    type = WhitelistItemType.CHANNEL,
                    youtubeId = "UCexisting",
                    title = "Existing Channel",
                    thumbnailUrl = "https://existing.jpg"
                )
            )
        }

        val originalProfiles = db.kidProfileDao().getProfilesByParent(PARENT_ID).first()
        val originalItems = db.whitelistItemDao().getItemsByProfile(existingProfileId).first()

        // Valid ExportData JSON, but one item has a type that WhitelistItemType.valueOf can't
        // parse - this throws mid-way through the import, after OVERWRITE has already deleted
        // the existing profiles in-memory (but, with the fix, inside an uncommitted transaction).
        val badJson = json.encodeToString(
            ExportData.serializer(),
            ExportData(
                version = 1,
                exportedAt = 2_000L,
                profiles = listOf(
                    ExportProfile(
                        name = "New Kid",
                        avatarUrl = null,
                        dailyLimitMinutes = null,
                        sleepPlaylistId = null,
                        whitelistItems = listOf(
                            ExportWhitelistItem(
                                type = "NOT_A_TYPE",
                                youtubeId = "UCnew",
                                title = "New Channel",
                                thumbnailUrl = "https://new.jpg"
                            )
                        )
                    )
                )
            )
        )

        val result = service.importFromJson(PARENT_ID, badJson, ImportStrategy.OVERWRITE)

        assertThat(result).isInstanceOf(AppResult.Error::class.java)

        val afterProfiles = db.kidProfileDao().getProfilesByParent(PARENT_ID).first()
        val afterItems = db.whitelistItemDao().getItemsByProfile(existingProfileId).first()

        // Rollback must have restored the pre-delete state exactly - nothing destroyed.
        assertThat(afterProfiles).isEqualTo(originalProfiles)
        assertThat(afterItems).isEqualTo(originalItems)
    }

    @Test
    fun `import rejects malicious youtubeId and does not persist it`() = runTest {
        // B1 regression guard: a crafted backup .json with a JS-injection payload as the
        // youtubeId must never reach the DB - if it did, it would later be string-
        // interpolated into the kid player's WebView JavaScript and executed as script.
        val maliciousJson = json.encodeToString(
            ExportData.serializer(),
            ExportData(
                version = 1,
                exportedAt = 1_000L,
                profiles = listOf(
                    ExportProfile(
                        name = "Kid A",
                        whitelistItems = listOf(
                            ExportWhitelistItem(
                                type = "VIDEO",
                                youtubeId = "');alert(1)//",
                                title = "Evil Video",
                                thumbnailUrl = "https://thumb.jpg"
                            ),
                            ExportWhitelistItem(
                                type = "CHANNEL",
                                youtubeId = "UC1234567890123456789012",
                                title = "Good Channel",
                                thumbnailUrl = "https://thumb.jpg"
                            )
                        )
                    )
                )
            )
        )

        val result = service.importFromJson(PARENT_ID, maliciousJson, ImportStrategy.MERGE)

        val importResult = (result as AppResult.Success).data
        assertThat(importResult.itemsImported).isEqualTo(1)
        assertThat(importResult.itemsSkipped).isEqualTo(1)

        val profile = db.kidProfileDao().getProfilesByParent(PARENT_ID).first().single()
        val items = db.whitelistItemDao().getItemsByProfile(profile.id).first()
        assertThat(items).hasSize(1)
        assertThat(items.none { it.youtubeId == "');alert(1)//" }).isTrue()
        assertThat(items.single().youtubeId).isEqualTo("UC1234567890123456789012")
    }

    companion object {
        private const val PARENT_ID = "parent-1"
    }
}
