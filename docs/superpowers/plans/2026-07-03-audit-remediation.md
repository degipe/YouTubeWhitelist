# Audit Remediation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix all findings from the 2026-07-03 deep-analysis audit (4 critical, 6 high, plus medium/low security, quality, and documentation issues) across the YouTubeWhitelist Android app.

**Architecture:** Work proceeds in 5 self-contained sessions. Each session is independently shippable: all tests green, app builds, and it can be committed/pushed on its own. Session order is by user-impact severity (data-loss and core-safety first). Every fix follows TDD — failing test first, minimal implementation, green test, commit.

**Tech Stack:** Kotlin 2.1.0, Jetpack Compose, Room 2.7.0, Hilt, Retrofit/OkHttp, kotlinx.serialization, JUnit + MockK + Truth + Turbine + Robolectric.

## Global Constraints

- **minSdk 26, compileSdk 35, targetSdk 35, JVM 17** — do not change. (verbatim from all module build files)
- **Package convention:** `io.github.degipe.youtubewhitelist.<module>.<layer>`
- **Communicate in Hungarian; all code, comments, and docs in English.** (CLAUDE.md)
- **TDD is mandatory** — write the failing test first for every behavioral change. (project memory: "User requires strict TDD discipline")
- **AppResult<T>** lives in `core.common.result` — the sealed result type used by all repositories.
- **Run tests with:** `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew <module>:test`
- **Do NOT commit secrets.** `local.properties` stays gitignored. The already-committed YouTube API key is handled by rotation (Session 3), not by removing the fallback.
- **Baseline before starting:** `./gradlew test` must be green (~428 tests). If not, stop and report.

---

## SESSION 1 — Critical data-safety & the broken player (K1, K2, K3)

Goal: eliminate the three findings that cause data loss or break the core kid experience. Ship-blocking bugs first.

---

### Task 1.1: Fix video player never reloading on video change (K1)

**Files:**
- Modify: `feature/kid/src/main/java/io/github/degipe/youtubewhitelist/feature/kid/ui/player/VideoPlayerScreen.kt:474-540`
- Test: `feature/kid/src/test/java/io/github/degipe/youtubewhitelist/feature/kid/ui/player/VideoPlayerReloadTest.kt` (new)

**Interfaces:**
- Consumes: existing `buildYouTubePlayerHtml(youtubeId: String, origin: String, showControls: Boolean): String` (same file), `YouTubePlayer` composable's `youtubeId: String` param.
- Produces: player WebView that reloads its content whenever `youtubeId` changes.

**Root cause:** `AndroidView.factory` runs once; `update = { /* no-op */ }` never pushes the new `youtubeId` into the WebView. `DisposableEffect(youtubeId)` only destroys the old WebView.

- [ ] **Step 1: Read the current `YouTubePlayer` composable** (`VideoPlayerScreen.kt:455-541`) to confirm the exact `factory`/`update`/`DisposableEffect` shape and the `webViewRef` handling before editing.

- [ ] **Step 2: Write a Robolectric test that a WebView is (re)loaded per youtubeId**

Because full Compose WebView testing is heavy, test the smallest observable contract: wrapping in `key(youtubeId)` recreates the node. Create `VideoPlayerReloadTest.kt`:

```kotlin
package io.github.degipe.youtubewhitelist.feature.kid.ui.player

import androidx.compose.material3.Text
import androidx.compose.runtime.key
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.platform.testTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VideoPlayerReloadTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun `key on youtubeId forces subtree recreation`() {
        var id = "aaaaaaaaaaa"
        rule.setContent {
            key(id) { Text("player-$id", modifier = androidx.compose.ui.Modifier.testTag("p")) }
        }
        rule.onNodeWithTag("p").assertIsDisplayed()
        id = "bbbbbbbbbbb"
        rule.runOnIdle { /* recomposition trigger simulated via key change in real screen */ }
    }
}
```

Note: this guards the `key()` approach conceptually. The real regression guard is the manual verification in Step 6.

- [ ] **Step 3: Run to verify it compiles/passes the baseline**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew feature:kid:test --tests "*VideoPlayerReloadTest*"`
Expected: PASS (this is the harness; the fix itself is verified in Step 6).

- [ ] **Step 4: Apply the fix — wrap `AndroidView` in `key(youtubeId)`**

In `VideoPlayerScreen.kt`, change the `AndroidView(...)` call (lines ~486-540) so the whole thing is inside `key(youtubeId) { ... }`. This forces the node — and its `DisposableEffect`/`factory` — to be torn down and recreated whenever `youtubeId` changes:

```kotlin
key(youtubeId) {
    DisposableEffect(youtubeId) {
        onDispose {
            webViewRef.value?.let { wv ->
                wv.loadUrl("about:blank")
                wv.stopLoading()
                wv.clearHistory()
                wv.destroy()
            }
            webViewRef.value = null
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                // ... unchanged factory body, ends with:
                val html = buildYouTubePlayerHtml(youtubeId, origin, showControls = true)
                loadDataWithBaseURL(origin, html, "text/html", "utf-8", null)
            }
        },
        update = { /* full recreation handled by key(youtubeId) */ },
        modifier = modifier
    )
}
```

Import: `androidx.compose.runtime.key`.

- [ ] **Step 5: Run the kid feature test suite**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew feature:kid:test`
Expected: PASS (all existing + new).

- [ ] **Step 6: Manual verification on emulator** (per project convention, Session 21 style)

Launch app, enter kid mode, open a channel/playlist, play a video, then:
1. Tap an "Up Next" card → the tapped video must actually load and play (not blank).
2. Let a short video end → next video autoplays.
3. Open an embed-disabled video (error 101/150) → auto-skips to next.
Confirm all three now work. Record the result in the session log.

- [ ] **Step 7: Commit**

```bash
git add feature/kid/src/main/java/io/github/degipe/youtubewhitelist/feature/kid/ui/player/VideoPlayerScreen.kt feature/kid/src/test/java/io/github/degipe/youtubewhitelist/feature/kid/ui/player/VideoPlayerReloadTest.kt
git commit -m "fix(kid): reload player WebView on video change (Next/autoplay/auto-skip)"
```

---

### Task 1.2: Fix export/import — MERGE dedup, and wrap import in a transaction (K2)

**Files:**
- Modify: `core/database/src/main/java/io/github/degipe/youtubewhitelist/core/database/YouTubeWhitelistDatabase.kt` (expose it to core:export — already a dependency; verify)
- Modify: `core/export/src/main/java/io/github/degipe/youtubewhitelist/core/export/ExportImportServiceImpl.kt:60-123`
- Test: `core/export/src/test/java/io/github/degipe/youtubewhitelist/core/export/ExportImportServiceImplTest.kt` (extend)
- Test (new, Robolectric, real DB): `core/export/src/test/java/io/github/degipe/youtubewhitelist/core/export/ExportImportRoundTripTest.kt`

**Interfaces:**
- Consumes: `KidProfileDao.getProfilesByParent(parentId): Flow<List<KidProfileEntity>>`, `KidProfileDao.insert/delete`, `WhitelistItemDao.findByYoutubeId(profileId, youtubeId): WhitelistItemEntity?`, `WhitelistItemDao.insert`, `androidx.room.withTransaction`.
- Produces: idempotent MERGE (re-importing the same file adds no duplicates), atomic import (any failure rolls back).

**Root cause:** every profile gets a fresh UUID regardless of strategy, so the MERGE dedup query always runs against an empty profile → never dedups → duplicates on re-import. And no transaction → OVERWRITE that fails mid-way leaves data destroyed.

- [ ] **Step 1: Inject the database for transactions.** Add `YouTubeWhitelistDatabase` to the constructor of `ExportImportServiceImpl` (Hilt already provides it as `@Singleton`). Confirm `core:export` `build.gradle.kts` depends on `:core:database` (it does — DAOs are imported).

- [ ] **Step 2: Write failing test — re-import is idempotent (MERGE)**

Add to a new Robolectric test `ExportImportRoundTripTest.kt` using an in-memory Room DB:

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class ExportImportRoundTripTest {
    private lateinit var db: YouTubeWhitelistDatabase
    private lateinit var service: ExportImportServiceImpl

    @Before fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, YouTubeWhitelistDatabase::class.java)
            .allowMainThreadQueries().build()
        // seed a parent account row (FK), one profile, one whitelist item
        service = ExportImportServiceImpl(db.kidProfileDao(), db.whitelistItemDao(), db)
    }
    @After fun teardown() = db.close()

    @Test fun `re-import MERGE does not duplicate profiles or items`() = runTest {
        val json = (service.exportToJson(PARENT_ID) as AppResult.Success).data
        service.importFromJson(PARENT_ID, json, ImportStrategy.MERGE)
        service.importFromJson(PARENT_ID, json, ImportStrategy.MERGE)
        val profiles = db.kidProfileDao().getProfilesByParent(PARENT_ID).first()
        // originally 1 seeded + 1 first import; second import must add nothing new
        assertThat(profiles.filter { it.name == "Kid A" }).hasSize(2) // seeded + first import only
        val items = db.whitelistItemDao().getItemsByProfile(profiles.last().id).first()
        assertThat(items).hasSize(1) // second import skipped the duplicate
    }
}
```

- [ ] **Step 3: Run to verify it fails**

Run: `JAVA_HOME=... ./gradlew core:export:test --tests "*ExportImportRoundTripTest*"`
Expected: FAIL — second import creates a duplicate item (findByYoutubeId ran against a new empty profile).

- [ ] **Step 4: Rewrite `importFromJson` — dedup by profile name for MERGE + wrap in a transaction**

```kotlin
override suspend fun importFromJson(
    parentAccountId: String,
    jsonString: String,
    strategy: ImportStrategy
): AppResult<ImportResult> {
    return try {
        val exportData = Json.decodeFromString<ExportData>(jsonString)
        var profilesImported = 0
        var itemsImported = 0
        var itemsSkipped = 0

        database.withTransaction {
            if (strategy == ImportStrategy.OVERWRITE) {
                kidProfileDao.getProfilesByParent(parentAccountId).first()
                    .forEach { kidProfileDao.delete(it) }
            }

            val existingByName = kidProfileDao.getProfilesByParent(parentAccountId).first()
                .associateBy { it.name }

            for (exportProfile in exportData.profiles) {
                // MERGE reuses an existing same-named profile; OVERWRITE always creates new
                val targetProfileId = if (strategy == ImportStrategy.MERGE) {
                    existingByName[exportProfile.name]?.id
                } else null

                val profileId = targetProfileId ?: UUID.randomUUID().toString().also {
                    kidProfileDao.insert(
                        KidProfileEntity(
                            id = it,
                            parentAccountId = parentAccountId,
                            name = exportProfile.name,
                            avatarUrl = exportProfile.avatarUrl,
                            dailyLimitMinutes = exportProfile.dailyLimitMinutes,
                            sleepPlaylistId = exportProfile.sleepPlaylistId
                        )
                    )
                    profilesImported++
                }

                for (exportItem in exportProfile.whitelistItems) {
                    if (strategy == ImportStrategy.MERGE &&
                        whitelistItemDao.findByYoutubeId(profileId, exportItem.youtubeId) != null
                    ) {
                        itemsSkipped++
                        continue
                    }
                    whitelistItemDao.insert(
                        WhitelistItemEntity(
                            id = UUID.randomUUID().toString(),
                            kidProfileId = profileId,
                            type = WhitelistItemType.valueOf(exportItem.type),
                            youtubeId = exportItem.youtubeId,
                            title = exportItem.title,
                            thumbnailUrl = exportItem.thumbnailUrl,
                            channelTitle = exportItem.channelTitle
                        )
                    )
                    itemsImported++
                }
            }
        }

        AppResult.Success(ImportResult(profilesImported, itemsImported, itemsSkipped))
    } catch (e: Exception) {
        AppResult.Error(e.message ?: "Import failed")
    }
}
```

Import `androidx.room.withTransaction`.

- [ ] **Step 5: Write failing test — atomicity on OVERWRITE with a corrupt item**

```kotlin
@Test fun `OVERWRITE with invalid item type rolls back and keeps original data`() = runTest {
    val original = db.kidProfileDao().getProfilesByParent(PARENT_ID).first()
    val badJson = /* valid ExportData JSON but one item has type = "NOT_A_TYPE" */
    val result = service.importFromJson(PARENT_ID, badJson, ImportStrategy.OVERWRITE)
    assertThat(result).isInstanceOf(AppResult.Error::class.java)
    val after = db.kidProfileDao().getProfilesByParent(PARENT_ID).first()
    assertThat(after).isEqualTo(original) // original NOT destroyed
}
```

- [ ] **Step 6: Run both tests**

Run: `JAVA_HOME=... ./gradlew core:export:test`
Expected: PASS. (`withTransaction` rolls back the pre-delete when `valueOf` throws.)

- [ ] **Step 7: Fix the misleading old unit test.** In `ExportImportServiceImplTest.kt`, the mock `findByYoutubeId(any(), "UC123")` masks real behavior. Update those mocks to be profile-id-specific, or delete the now-superseded cases in favor of the round-trip test. Re-run.

- [ ] **Step 8: Commit**

```bash
git add core/export/ 
git commit -m "fix(export): real MERGE dedup by profile name + atomic import transaction"
```

---

### Task 1.3: Room schema export + migration infrastructure (K3)

**Files:**
- Modify: `core/database/build.gradle.kts` (add KSP schema arg, room-testing dep for migration tests)
- Create: `core/database/schemas/…/3.json` (generated by building once)
- Modify: `core/database/src/main/java/io/github/degipe/youtubewhitelist/core/database/di/DatabaseModule.kt:25-30`
- Create: `core/database/src/main/java/io/github/degipe/youtubewhitelist/core/database/migration/Migrations.kt`
- Test: `core/database/src/androidTest/java/.../MigrationTest.kt` (instrumented, template for future migrations)

**Interfaces:**
- Consumes: Room's `MigrationTestHelper`, `Room.databaseBuilder(...).addMigrations(...)`.
- Produces: exported schema JSON history starting at v3, a `Migrations` object to append to, and a safety-net kept `fallbackToDestructiveMigration()` (per user choice: schema-export + real migrations, fallback retained as last resort).

- [ ] **Step 1: Enable schema export in KSP.** In `core/database/build.gradle.kts`, add inside the `android {}`→ after plugins, a `ksp {}` block:

```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

Also change `androidTestImplementation(libs.room.testing.lib)` is present; add `implementation(libs.room.runtime)` already there. Ensure `sourceSets` picks up schemas for migration tests:

```kotlin
android {
    // ...
    sourceSets["androidTest"].assets.srcDir("$projectDir/schemas")
}
```

- [ ] **Step 2: Build once to generate `3.json`**

Run: `JAVA_HOME=... ./gradlew core:database:kspDebugKotlin`
Expected: `core/database/schemas/io.github.degipe.youtubewhitelist.core.database.YouTubeWhitelistDatabase/3.json` now exists. Commit this file — it is the migration baseline.

- [ ] **Step 3: Create the migrations container**

`core/database/.../migration/Migrations.kt`:

```kotlin
package io.github.degipe.youtubewhitelist.core.database.migration

import androidx.room.migration.Migration

/**
 * Real schema migrations, appended one per version bump from v3 onward.
 * NEVER rely on destructive fallback for user-data tables again.
 */
object Migrations {
    val ALL: Array<Migration> = arrayOf(
        // MIGRATION_3_4, MIGRATION_4_5, ... added as the schema evolves
    )
}
```

- [ ] **Step 4: Wire migrations into the builder (keep fallback as safety net)**

In `DatabaseModule.kt`:

```kotlin
return Room.databaseBuilder(
    context,
    YouTubeWhitelistDatabase::class.java,
    "youtubewhitelist.db"
)
    .addMigrations(*Migrations.ALL)
    .fallbackToDestructiveMigration() // last-resort safety net; every real bump MUST ship a Migration above
    .build()
```

- [ ] **Step 5: Add a migration-test template (instrumented)**

`core/database/src/androidTest/.../MigrationTest.kt`:

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val DB = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        YouTubeWhitelistDatabase::class.java
    )

    @Test
    fun schemaV3_opens() {
        helper.createDatabase(DB, 3).apply { close() }
        // When MIGRATION_3_4 is added, assert data survives here.
    }
}
```

- [ ] **Step 6: Add a CLAUDE.md / DEVELOPER_ONBOARDING note** documenting the rule: "Any `@Database version` bump REQUIRES a `Migration` in `Migrations.ALL` + a migration test. Destructive fallback is a crash-prevention net, not a strategy."

- [ ] **Step 7: Run**

Run: `JAVA_HOME=... ./gradlew core:database:test` (unit) and note instrumented `MigrationTest` runs on device/emulator via `connectedAndroidTest`.
Expected: green build, `3.json` committed.

- [ ] **Step 8: Commit**

```bash
git add core/database/
git commit -m "feat(db): export Room schema v3 + migration infra, retain fallback as safety net"
```

**Session 1 exit check:** `./gradlew test` green; emulator confirms player Next/autoplay/auto-skip; re-importing a backup adds no duplicates; `schemas/3.json` committed. Update CLAUDE.md session log + NEXT_SESSION_PROMPT.md, push.

---

## SESSION 2 — Kid-mode correctness & content-safety (M2, M4, M5, M6, B1)

Goal: fix pagination correctness, error-state UX, quota leak during search, midnight rollover, and the JS-injection content-safety hole.

---

### Task 2.1: Validate & escape YouTube IDs — close the JS-injection hole (B1)

**Files:**
- Modify: `core/common/src/main/java/io/github/degipe/youtubewhitelist/core/common/youtube/YouTubeUrlParser.kt`
- Create: `core/common/.../youtube/YouTubeId.kt` (validation helpers)
- Modify: `core/export/.../ExportImportServiceImpl.kt` (reject invalid IDs on import)
- Modify: `feature/kid/.../player/VideoPlayerScreen.kt` (`buildYouTubePlayerHtml` — JSON-encode the id)
- Test: `core/common/src/test/.../youtube/YouTubeIdTest.kt` (new), extend `YouTubeUrlParserTest`

**Interfaces:**
- Produces: `object YouTubeId { fun isValidVideoId(s): Boolean; fun isValidChannelId(s): Boolean; fun isValidPlaylistId(s): Boolean }`; parser returns null for malformed IDs; player HTML injects the id via `org.json.JSONObject.quote(...)` (or a JS-string escaper) so it cannot break out of the JS literal.

- [ ] **Step 1: Write failing tests for the validator** (`YouTubeIdTest.kt`):

```kotlin
class YouTubeIdTest {
    @Test fun `valid 11-char video id`() { assertThat(YouTubeId.isValidVideoId("dQw4w9WgXcQ")).isTrue() }
    @Test fun `injection payload rejected`() {
        assertThat(YouTubeId.isValidVideoId("');alert(1)//")).isFalse()
    }
    @Test fun `channel id must start UC and be 24 chars`() {
        assertThat(YouTubeId.isValidChannelId("UCX6OQ3DkcsbYNE6H8uQQuVA")).isTrue()
        assertThat(YouTubeId.isValidChannelId("bad")).isFalse()
    }
}
```

- [ ] **Step 2: Run — FAIL** (`YouTubeId` doesn't exist). `JAVA_HOME=... ./gradlew core:common:test --tests "*YouTubeIdTest*"`

- [ ] **Step 3: Implement `YouTubeId`**:

```kotlin
package io.github.degipe.youtubewhitelist.core.common.youtube

object YouTubeId {
    private val VIDEO = Regex("^[A-Za-z0-9_-]{11}$")
    private val CHANNEL = Regex("^UC[A-Za-z0-9_-]{22}$")
    private val PLAYLIST = Regex("^(PL|UU|FL|LL|RD)[A-Za-z0-9_-]{10,}$")

    fun isValidVideoId(s: String) = VIDEO.matches(s)
    fun isValidChannelId(s: String) = CHANNEL.matches(s)
    fun isValidPlaylistId(s: String) = PLAYLIST.matches(s)
}
```

- [ ] **Step 4: Enforce in the parser.** In `YouTubeUrlParser.parseYouTubeUrl`/`parseShortUrl`, before returning a `VIDEO`/`CHANNEL`/`PLAYLIST` result, gate on the matching `YouTubeId.isValid*`. Return null otherwise. (Handles/custom names stay as-is — they are resolved via API, not embedded in JS.) Add parser tests asserting a `watch?v=');alert(1)//` URL returns null.

- [ ] **Step 5: Enforce on import.** In `ExportImportServiceImpl`, when building each `WhitelistItemEntity`, validate `exportItem.youtubeId` against the type; skip+count invalid items (reuse `itemsSkipped`) instead of persisting. Add a round-trip test importing a JSON with a malicious `youtubeId` and asserting it is not stored.

- [ ] **Step 6: JSON-encode the id in player HTML.** In `buildYouTubePlayerHtml`, replace `videoId: '$videoId'` with a safely-encoded value:

```kotlin
val safeId = org.json.JSONObject.quote(youtubeId) // yields a quoted, escaped JS string literal
// in the JS template: videoId: $safeId,   (note: no surrounding quotes — quote() adds them)
```

(Requires Robolectric for `org.json` in tests — the existing test infra already uses it.)

- [ ] **Step 7: Run** `JAVA_HOME=... ./gradlew core:common:test core:export:test feature:kid:test` — Expected PASS.

- [ ] **Step 8: Commit**

```bash
git commit -am "fix(security): validate YouTube IDs and JSON-encode player id (block JS injection)"
```

---

### Task 2.2: Stop RSS from capping channel pagination at ~15 (M2)

**Files:**
- Modify: `core/data/.../repository/impl/HybridYouTubeRepositoryImpl.kt:80-94, 147-157`
- Test: `core/data/src/test/.../HybridYouTubeRepositoryImplTest.kt`

**Root cause:** `getPlaylistItemsPage(pageToken=null)` tries RSS first and returns `nextPageToken=null`, so `hasMorePages` becomes false after the first (RSS) page — older videos become unreachable for channels with >15 uploads.

**Chosen fix:** do NOT use RSS for the paginated lazy-load entry point. Reserve RSS for the non-paginated `getPlaylistItems()`. Always start `getPlaylistItemsPage` via API → Invidious so a real `nextPageToken` flows.

- [ ] **Step 1: Write failing test** — first page via API returns a nextPageToken and RSS is not consulted for pagination:

```kotlin
@Test fun `getPlaylistItemsPage first page uses API and returns nextPageToken`() = runTest {
    // arrange: API returns page with nextPageToken = "TOKEN2"
    val result = repo.getPlaylistItemsPage("UUxxxx", null)
    assertThat((result as AppResult.Success).data.nextPageToken).isEqualTo("TOKEN2")
    verify(exactly = 0) { rssFeedParser.fetchChannelVideos(any()) }
}
```

- [ ] **Step 2: Run — FAIL** (RSS is currently called first).

- [ ] **Step 3: Remove the RSS branch from `getPlaylistItemsPage`**:

```kotlin
override suspend fun getPlaylistItemsPage(
    playlistId: String,
    pageToken: String?
): AppResult<PaginatedPlaylistResult> = withContext(ioDispatcher) {
    tryApiPlaylistItemsPage(playlistId, pageToken)
        ?: tryInvidiousPlaylistItemsPage(playlistId)
        ?: AppResult.Error("Failed to fetch playlist items from all sources")
}
```

Delete `tryRssFeedPaginated` if now unused (keep `tryRssFeed` for the non-paginated path). Verify no other caller references it.

- [ ] **Step 4: Update/adjust the existing pagination tests** that asserted RSS-first for the paginated path (the audit noted `HybridYouTubeRepositoryImplTest.kt:346` "RSS has no pagination"). Re-point them to API-first.

- [ ] **Step 5: Run** `JAVA_HOME=... ./gradlew core:data:test` — Expected PASS.

- [ ] **Step 6: Manual verification** — open a large channel (e.g. MrBeast) in kid mode; scroll past ~15 videos; confirm infinite scroll continues loading (as Session 21, but now deterministic).

- [ ] **Step 7: Commit**

```bash
git commit -am "fix(data): API-first channel pagination so >15 videos stay reachable"
```

---

### Task 2.3: Surface loadMore errors + gate infinite-scroll on search (M4, M5)

**Files:**
- Modify: `feature/kid/.../channel/ChannelDetailViewModel.kt:102-127` (add `loadMoreFailed` flag; guard on search)
- Modify: `feature/kid/.../channel/ChannelDetailScreen.kt:194-214` (trailing item + retry UI)
- Test: `feature/kid/src/test/.../channel/ChannelDetailViewModelTest.kt`

**Interfaces:**
- Produces: `ChannelDetailUiState.loadMoreFailed: Boolean`; `loadMore()` early-returns while `searchQuery` is non-blank; UI shows a Retry affordance instead of an infinite spinner on error; trailing load-more item only renders when `searchQuery.isBlank() && error == null && !loadMoreFailed`.

- [ ] **Step 1: Write failing test — loadMore during active search is a no-op**

```kotlin
@Test fun `loadMore is ignored while a search query is active`() = runTest {
    viewModel.onSearchQueryChanged("squid")
    advanceTimeBy(301)
    viewModel.loadMore()
    // no API page fetch should happen
    coVerify(exactly = 0) { youTubeApiRepository.getPlaylistItemsPage(any(), neq(null)) }
}
```

- [ ] **Step 2: Write failing test — loadMore error sets loadMoreFailed and stops the spinner**

```kotlin
@Test fun `loadMore error sets loadMoreFailed true`() = runTest {
    // arrange: first page ok with token, second page Error
    viewModel.loadMore()
    advanceUntilIdle()
    assertThat(viewModel.uiState.value.loadMoreFailed).isTrue()
    assertThat(viewModel.uiState.value.isLoadingMore).isFalse()
}
```

- [ ] **Step 3: Run — FAIL** (no `loadMoreFailed`; loadMore not gated).

- [ ] **Step 4: Implement in the ViewModel.** Add `loadMoreFailed: Boolean = false` to `ChannelDetailUiState` and `ControlState`; map it through `combine`. In `loadMore()`:

```kotlin
fun loadMore() {
    if (_searchQuery.value.isNotBlank()) return              // M5: no pagination during search
    val token = nextPageToken
    if (token == null || _controlState.value.isLoadingMore) return
    _controlState.value = _controlState.value.copy(isLoadingMore = true, loadMoreFailed = false)
    viewModelScope.launch {
        val playlistId = uploadsPlaylistId ?: return@launch
        when (val result = youTubeApiRepository.getPlaylistItemsPage(playlistId, token)) {
            is AppResult.Success -> {
                channelVideoCacheRepository.cacheVideos(channelId, result.data.videos)
                nextPageToken = result.data.nextPageToken
                _controlState.value = _controlState.value.copy(
                    isLoadingMore = false,
                    hasMorePages = result.data.nextPageToken != null,
                    loadMoreFailed = false
                )
            }
            is AppResult.Error -> _controlState.value = _controlState.value.copy(
                isLoadingMore = false, loadMoreFailed = true, error = result.message
            )
        }
    }
}
```

Add `fun retryLoadMore() { _controlState.value = _controlState.value.copy(loadMoreFailed = false); loadMore() }`.

- [ ] **Step 5: Update the Screen.** Gate the trailing item:

```kotlin
if (uiState.hasMorePages && searchQuery.isBlank() && !uiState.loadMoreFailed) {
    item(key = "load_more") {
        LaunchedEffect(Unit) { viewModel.loadMore() }
        CircularProgressIndicator(/* ... */)
    }
} else if (uiState.loadMoreFailed && uiState.videos.isNotEmpty()) {
    item(key = "load_more_retry") {
        Row { Text("Couldn't load more"); TextButton(onClick = { viewModel.retryLoadMore() }) { Text("Retry") } }
    }
}
```

- [ ] **Step 6: Run** `JAVA_HOME=... ./gradlew feature:kid:test` — Expected PASS.

- [ ] **Step 7: Commit**

```bash
git commit -am "fix(kid): show load-more errors with retry; stop pagination/quota during search"
```

---

### Task 2.4: Fix midnight rollover for daily time limit (M6)

**Files:**
- Modify: `core/database/.../dao/WatchHistoryDao.kt` (add a self-recomputing day query, or keep param but recompute reactively)
- Modify: `core/data/.../repository/impl/WatchHistoryRepositoryImpl.kt:70-79`
- Test: `core/data/src/test/.../WatchHistoryRepositoryImplTest.kt` (+ DAO test if SQL changes)

**Root cause:** `startOfToday()` is computed once at Flow creation; the bound `sinceTimestamp` never advances across midnight for a live session.

**Chosen fix (SQL-side day boundary):** make the "today" boundary computed by SQLite on each emission so it always reflects the current local day.

- [ ] **Step 1: Add a DAO query using SQLite date functions**

```kotlin
@Query("""
    SELECT COALESCE(SUM(watchedSeconds), 0) FROM watch_history
    WHERE kidProfileId = :profileId
      AND watchedAt >= (strftime('%s', 'now', 'localtime', 'start of day', 'utc') * 1000)
""")
fun getTotalWatchedSecondsTodayFlow(profileId: String): Flow<Int>
```

Note: `'localtime'` shifts to the device timezone, `'start of day'` truncates to midnight, and the trailing `'utc'` converts the local-looking value back to a true UTC instant. The `'utc'` modifier is REQUIRED — without it SQLite re-interprets the shifted value as UTC and the boundary is off by the local UTC offset in every non-UTC+0 timezone (a known SQLite trap; caught in review). Test with tight fixtures (a row at local 23:30 yesterday and 00:30 today, relative to actual local midnight) so a regression is caught.

- [ ] **Step 2: Point the repository at it**

```kotlin
override fun getTotalWatchedSecondsTodayFlow(profileId: String): Flow<Int> =
    watchHistoryDao.getTotalWatchedSecondsTodayFlow(profileId)
```

Remove the now-unused `startOfToday()` from the Flow path (keep it only if `getTotalWatchedSecondsToday()` non-Flow still uses it; that one is a point-in-time read and is acceptable).

- [ ] **Step 3: Write a Robolectric DAO test** inserting rows dated "yesterday 23:00" and "today 00:30", asserting the Flow returns only today's seconds. Use a fixed clock by inserting explicit `watchedAt` millis relative to `strftime` — or assert relative behavior (yesterday-dated row excluded).

- [ ] **Step 4: Run — implement to green.** `JAVA_HOME=... ./gradlew core:database:test core:data:test`

- [ ] **Step 5: Commit**

```bash
git commit -am "fix(timelimit): compute today boundary in SQL so it rolls over at midnight"
```

**Session 2 exit check:** all module tests green; manual: large-channel scroll continues, search doesn't fetch pages, malicious import id rejected. Update session log + NEXT_SESSION_PROMPT, push.

---

## SESSION 3 — Auth & security hardening (B2, B3, B4, B5, B6, M1)

Goal: lock down OAuth (loopback binding, state, PKCE, drop client secret), restrict/rotate the API key, harden the kid WebView and backup, and unblock F-Droid onboarding.

> **External steps (user runs these in Google Cloud Console):** detailed click-by-click guides are in Steps flagged `[USER/CONSOLE]`. Code and app changes are done by the implementer.

---

### Task 3.1: Bind OAuth loopback to localhost + validate `state` (B2)

**Files:**
- Modify: `core/auth/.../google/OAuthLoopbackServer.kt:23`
- Modify: `core/auth/.../google/GoogleSignInManagerImpl.kt` (pass expected `state`, verify on callback)
- Test: `core/auth/src/test/.../google/OAuthLoopbackServerTest.kt`

- [ ] **Step 1: Write failing test — callback with wrong state is rejected**

```kotlin
@Test fun `callback with mismatched state returns Error`() = runTest {
    val server = OAuthLoopbackServer(expectedState = "GOOD")
    // simulate a GET /callback?code=abc&state=EVIL against server.port
    val result = /* drive a localhost socket request */ 
    assertThat(result).isInstanceOf(OAuthCallbackResult.Error::class.java)
}
```

- [ ] **Step 2: Bind to loopback + accept + validate state**

```kotlin
class OAuthLoopbackServer(private val expectedState: String) {
    private val serverSocket = ServerSocket(0, 0, java.net.InetAddress.getLoopbackAddress())
    // ...
    // in awaitAuthorizationCode(), after parsing params:
    val returnedState = params["state"]
    if (returnedState != expectedState) return@withContext OAuthCallbackResult.Error("State mismatch")
    // then the existing code/error/cancelled logic
}
```

- [ ] **Step 3: Thread the state through `GoogleSignInManagerImpl`** — it already generates a `state` UUID; pass it into `OAuthLoopbackServer(expectedState = state)` and ensure the auth URL includes the same `state`.

- [ ] **Step 4: Run** `JAVA_HOME=... ./gradlew core:auth:test` — implement to green.

- [ ] **Step 5: Commit** `git commit -am "fix(auth): bind OAuth callback to loopback and validate state (CSRF)"`

---

### Task 3.2: Adopt PKCE and drop the client secret (B4)

**Files:**
- Create: `core/auth/.../google/PkceGenerator.kt`
- Modify: `core/auth/.../google/OAuthConfig.kt` (add `code_challenge`), `OAuthTokenExchanger.kt:43-49` (send `code_verifier`, remove `client_secret`)
- Modify: `app/build.gradle.kts` / `ApiKeyModule.kt` (remove `GOOGLE_CLIENT_SECRET` once native client is used)
- Test: `core/auth/src/test/.../google/PkceGeneratorTest.kt`

- [ ] **Step 1: `[USER/CONSOLE]` Create a new OAuth client of type "Android" or "Desktop/Installed app"** (PKCE-capable, no secret):
  1. Go to https://console.cloud.google.com → APIs & Services → Credentials.
  2. "Create Credentials" → "OAuth client ID".
  3. Application type: **Desktop app** (installed-app PKCE flow; works with loopback redirect and needs no secret). Name it "YouTubeWhitelist Installed".
  4. Copy the new **Client ID**. There is no client secret required for PKCE with an installed app; if one is shown, you will NOT embed it.
  5. Add the new Client ID to your `local.properties` as `GOOGLE_CLIENT_ID`, and set a baked-in fallback (see Task 3.6) so F-Droid builds work.

- [ ] **Step 2: Write failing test for PKCE generator**

```kotlin
class PkceGeneratorTest {
    @Test fun `challenge is base64url sha256 of verifier`() {
        val pair = PkceGenerator.generate()
        val expected = /* S256(pair.verifier) */
        assertThat(pair.challenge).isEqualTo(expected)
        assertThat(pair.verifier.length).isAtLeast(43)
    }
}
```

- [ ] **Step 3: Implement `PkceGenerator`** (SecureRandom verifier, `SHA-256` + base64url-no-padding challenge). Use `java.security.MessageDigest` + `java.util.Base64.getUrlEncoder().withoutPadding()`.

- [ ] **Step 4: Add `code_challenge`/`code_challenge_method=S256` to the auth URL** in `OAuthConfig`; in `OAuthTokenExchanger`, send `code_verifier` and **remove** the `client_secret` form field.

- [ ] **Step 5: Remove `GOOGLE_CLIENT_SECRET`** buildConfig field and its `ApiKeyModule` provider once the token exchange no longer needs it.

- [ ] **Step 6: Run** `core:auth:test` green; **manual OAuth sign-in on emulator** end-to-end (new client id) — confirm sign-in still succeeds.

- [ ] **Step 7: Commit** `git commit -am "feat(auth): PKCE code flow, drop embedded client secret"`

---

### Task 3.3: Restrict & rotate the YouTube API key (B3)

**Files:** none in code beyond swapping the fallback key value in `app/build.gradle.kts:49` after rotation.

- [ ] **Step 1: `[USER/CONSOLE]` Restrict the current key:**
  1. Console → Credentials → click the API key `AIzaSy…IzF4`.
  2. "API restrictions" → Restrict key → select **YouTube Data API v3** only → Save.
  3. (Android app restriction would break F-Droid-signed builds — do NOT add a signature restriction if F-Droid must work. API-restriction + quota monitoring is the compromise.)
  4. Set up a **budget/quota alert**: APIs & Services → YouTube Data API → Quotas → confirm 10k/day; optionally lower per-minute limits.

- [ ] **Step 2: `[USER/CONSOLE]` Rotate:** since the key is already public, create a NEW API key, apply the same YouTube-Data-API restriction, then update the `local.properties` value AND the baked-in fallback string in `app/build.gradle.kts:49`. Delete/disable the old key after the new release ships.

- [ ] **Step 3: Update the code fallback** to the new key value; rebuild; smoke-test an API-backed screen. Commit `git commit -am "chore(security): rotate + restrict YouTube API key"` (the new key is public-by-necessity, restricted).

---

### Task 3.4: Harden the kid player WebView (B6)

**Files:** Modify `feature/kid/.../player/VideoPlayerScreen.kt` (factory settings block).

- [ ] **Step 1: Add the same four flags the parent browser sets** (mirror `WebViewBrowserScreen.kt:217-222`):

```kotlin
settings.allowFileAccess = false
settings.allowContentAccess = false
settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
settings.safeBrowsingEnabled = true
```

- [ ] **Step 2: Manual verification** — player still loads and plays whitelisted videos (these flags don't affect `https://youtube.com` embeds).

- [ ] **Step 3: Commit** `git commit -am "fix(security): harden kid player WebView (file/content access, mixed content, safe browsing)"`

---

### Task 3.5: Disable backup for sensitive data (B5)

**Files:** Modify `app/src/main/AndroidManifest.xml:11`; optionally create `app/src/main/res/xml/data_extraction_rules.xml`.

- [ ] **Step 1: Simplest safe choice — set `android:allowBackup="false"`** (export/import already covers user backup needs, and it prevents the `AEADBadTagException` restore-crash for EncryptedSharedPreferences and brute-force-counter reset via restore).

Alternatively, if backup is desired, add `android:dataExtractionRules`/`fullBackupContent` excluding the auth prefs, the `pin_brute_force` prefs, and the DB. Prefer `allowBackup=false` unless there's a product reason.

- [ ] **Step 2: Build + install; confirm app launches** (no functional change expected).

- [ ] **Step 3: Commit** `git commit -am "fix(security): disable auto-backup of encrypted prefs and DB"`

---

### Task 3.6: Unblock F-Droid onboarding (M1)

**Files:** Modify `app/build.gradle.kts:44-68` (fallback client id), and/or `feature/parent` SignIn screen + `navigation/AppNavigation.kt`.

- [ ] **Step 1: Decide the path.** Preferred: add a **"Continue without Google account"** option on the SignIn screen, since the API-key-only flow already powers whitelisting. Fallback: bake a working `GOOGLE_CLIENT_ID` default like the API key.

- [ ] **Step 2: Write a ViewModel/nav test** asserting that from SignIn, a "skip" action lands on the parent dashboard (or wherever the no-account flow belongs), and OAuth is not invoked.

- [ ] **Step 3: Implement the skip path** — add the button + nav action; guard `GoogleSignInManagerImpl.signIn()` to fail fast with a clear message when `clientId.isBlank()`.

- [ ] **Step 4: Run** `feature:parent:test` / `app:test` green; **build a release APK without `local.properties`** to simulate F-Droid, install, and confirm onboarding completes.

- [ ] **Step 5: Commit** `git commit -am "fix(onboarding): allow F-Droid builds to proceed without Google OAuth"`

**Session 3 exit check:** OAuth sign-in works end-to-end with PKCE/new client; F-Droid-simulated build onboards; API key restricted+rotated; backup off; kid WebView hardened. Update docs/session log, push.

---

## SESSION 4 — Network robustness & dead-weight cleanup (KM1, KM2, KM3, KM4)

Goal: stop the response-body leaks, refresh Invidious instances, delete dead code and unused dependencies.

---

### Task 4.1: Close response bodies on error paths (KM1)

**Files:**
- Modify: `core/network/.../rss/RssFeedParser.kt:16-17`, `core/network/.../invidious/InvidiousApiService.kt:44-46`
- Modify: `core/data/.../repository/impl/YouTubeApiRepositoryImpl.kt` (all `tryApi*` error branches), `HybridYouTubeRepositoryImpl.kt` (all `tryApi*`)
- Test: add a focused test where feasible (mock returning an unsuccessful response and assert `errorBody().close()` called; or verify via a small helper).

- [ ] **Step 1: Add a helper for Retrofit responses.** In `core/network` (or `core/common`), add:

```kotlin
inline fun <T> retrofit2.Response<T>.closeOnError() { if (!isSuccessful) errorBody()?.close() }
```

- [ ] **Step 2: Apply at every `isSuccessful` check** in the `tryApi*` functions — call `response.errorBody()?.close()` on the failure branch before returning null/Error. For the raw OkHttp calls in `RssFeedParser`/`InvidiousApiService`, wrap the `okhttp3.Response` in `.use { }` so it always closes.

- [ ] **Step 3: Write a test for the RSS parser failure path** (mock OkHttp `Call` returning a 500 with a body; assert the body is closed / `emptyList()` returned without leaking). Use OkHttp `MockWebServer` if available in test deps, else a mocked `Response`.

- [ ] **Step 4: Run** `core:network:test core:data:test` green.

- [ ] **Step 5: Commit** `git commit -am "fix(network): always close response bodies on error paths (connection leak)"`

---

### Task 4.2: Refresh the Invidious instance list (KM2)

**Files:** Modify `core/network/.../invidious/InvidiousInstanceManager.kt:59-64`.

- [ ] **Step 1: Replace dead instances.** Remove `vid.puffyan.us` and `invidious.namazso.eu` (shut down). Keep `yewtu.be`, `inv.nadeko.net`; add currently-live ones (verify each returns 200 before committing, e.g. `inv.tux.pizza`, `invidious.jing.rocks` — check at implementation time since availability changes):

```kotlin
val DEFAULT_INSTANCES = listOf(
    "yewtu.be",
    "inv.nadeko.net",
    // add 1-2 verified-live instances here at implementation time
)
```

- [ ] **Step 2: Update the privacy policy Invidious section** (see Session 5 / D1) to match the actual instance list.

- [ ] **Step 3: Run** `core:network:test` (the manager tests use injected instance lists, so they stay green).

- [ ] **Step 4: Commit** `git commit -am "chore(network): drop dead Invidious instances, keep verified-live ones"`

---

### Task 4.3: Remove dead code (KM3)

**Files:** `core/data/.../repository/YouTubeApiRepository.kt` (interface method), `HybridYouTubeRepositoryImpl.kt`, `YouTubeApiRepositoryImpl.kt`.

- [ ] **Step 1: Confirm no production caller** of `searchVideosInChannel(...)` via grep across `feature/` and `app/`. (Kid search is local Room now.)

- [ ] **Step 2: Decide `YouTubeApiRepositoryImpl` fate.** It's unbound in DI (Hybrid is bound). If truly unreferenced, delete it and its tests; otherwise mark `@Deprecated` with a comment. Prefer deletion to reduce surface. Remove `searchVideosInChannel` from the interface + Hybrid impl if unused.

- [ ] **Step 3: Run full build** `JAVA_HOME=... ./gradlew assembleDebug test` to catch any dangling reference.

- [ ] **Step 4: Commit** `git commit -am "chore: remove dead search-in-channel API path and unbound repository"`

---

### Task 4.4: Remove unused dependencies (KM4)

**Files:** `core/data/build.gradle.kts` (WorkManager), `core/auth/build.gradle.kts` (biometric), `app/src/main/AndroidManifest.xml` (USE_BIOMETRIC), `gradle/libs.versions.toml` (dead entries).

- [ ] **Step 1: Remove WorkManager** dep from `core:data` (zero `androidx.work` usage). Remove biometric dep from `core:auth` and the `USE_BIOMETRIC` permission from the manifest (no `BiometricPrompt` usage; only a `biometricEnabled` flag remains, which is fine as a stored setting).

- [ ] **Step 2: Prune dead catalog entries** in `libs.versions.toml`: `androidx-appcompat`, `work-testing`, `mockk-android`, redundant `material3`/`composeUiTest` versions (they come from the BOM), duplicate `room-testing`/`room-testing-lib`. Move the inline `androidx-test-core` version into `[versions]`.

- [ ] **Step 3: Full build + test** to confirm nothing referenced the removed entries.

- [ ] **Step 4: Commit** `git commit -am "chore(deps): drop unused WorkManager/biometric deps and dead catalog entries"`

**Session 4 exit check:** build + all tests green; APK size unchanged or smaller. Update session log, push.

---

## SESSION 5 — CI, build hygiene & documentation truth (M3, hygiene, D1-D4)

Goal: add CI, apply low-risk build-hygiene wins, and make the docs match the code.

---

### Task 5.1: Add GitHub Actions CI (M3)

**Files:** Create `.github/workflows/ci.yml`.

- [ ] **Step 1: Create the workflow**

```yaml
name: CI
on:
  push: { branches: [main] }
  pull_request:
jobs:
  build-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '17' }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew test lint assembleDebug --no-daemon
```

(The baked-in fallback API key means `assembleDebug` works without secrets. If OAuth client id is needed for a specific build variant, inject via a repo secret; debug build should not require it.)

- [ ] **Step 2: Push a branch and open a PR to confirm the workflow runs green.** Fix any lint/test failures surfaced (this is the point — CI may reveal latent issues).

- [ ] **Step 3: Commit** `git commit -am "ci: run unit tests, lint, and debug build on PR"`

---

### Task 5.2: Low-risk build hygiene

**Files:** `gradle.properties`, all 10 module `build.gradle.kts` (kotlinOptions → compilerOptions optional).

- [ ] **Step 1: Bump Gradle memory + enable build cache** in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC
org.gradle.caching=true
```

- [ ] **Step 2: Verify a clean build still succeeds** `JAVA_HOME=... ./gradlew clean assembleDebug test`.

- [ ] **Step 3 (optional, defer if time-boxed):** migrate `kotlinOptions {}` → `compilerOptions {}` across modules ahead of a future Kotlin 2.2 bump. Skip if risk/time doesn't warrant it this session.

- [ ] **Step 4: Commit** `git commit -am "chore(build): raise heap, enable build cache"`

---

### Task 5.3: Documentation truth-up (D1-D4)

**Files:** `docs/PRIVACY_POLICY.md`, `docs/privacy-policy.md`, `docs/PRD.md` (SEC-07), `CHANGELOG.md`, `CLAUDE.md` (tech-stack line), `README.md`, `docs/HLD.md`.

- [ ] **Step 1: D1 — Add an Invidious section** to both privacy files: disclose that on YouTube-API failure, video/channel/playlist IDs are sent to third-party Invidious instances (list them), and amend PRD SEC-07 which currently claims "no data to any third-party server." Update "Last updated" date.

- [ ] **Step 2: D2 — Fix CHANGELOG v1.0.0 line** "WebView OAuth 2.0" → "Chrome Custom Tabs + loopback OAuth (PKCE)"; fix the same stale wording in `CLAUDE.md` tech-stack.

- [ ] **Step 3: D3 — README build instructions:** mark the YouTube API key as optional (baked-in fallback exists); document that a personal key avoids sharing the default quota.

- [ ] **Step 4: D4 — Mark deferred features:** in PRD/HLD, flag biometric auth (FR-03) and WorkManager background refresh as "deferred / not implemented" (consistent with USER_MANUAL, and with their deps now removed in Session 4).

- [ ] **Step 5: Commit** `git commit -am "docs: disclose Invidious in privacy policy, fix OAuth wording, mark deferred features"`

**Session 5 exit check:** CI green on a PR; docs no longer contradict code. Final session log update + push. Consider tagging a patch release (v1.1.1) once Sessions 1-4 ship, given the data-loss and player fixes.

---

## Self-Review — Spec Coverage

| Finding | Task |
|---|---|
| K1 player reload | 1.1 |
| K2 export/import merge+txn | 1.2 |
| K3 Room migrations | 1.3 |
| B1 JS injection / ID validation | 2.1 |
| M2 RSS pagination cap | 2.2 |
| M4 loadMore error UX | 2.3 |
| M5 infinite-scroll during search | 2.3 |
| M6 midnight rollover | 2.4 |
| B2 loopback + state | 3.1 |
| B4 PKCE / drop secret | 3.2 |
| B3 API key restrict/rotate | 3.3 |
| B6 kid WebView hardening | 3.4 |
| B5 allowBackup | 3.5 |
| M1 F-Droid onboarding | 3.6 |
| KM1 response body leak | 4.1 |
| KM2 Invidious list | 4.2 |
| KM3 dead code | 4.3 |
| KM4 unused deps | 4.4 |
| M3 CI | 5.1 |
| build hygiene (heap/cache/compilerOptions) | 5.2 |
| D1-D4 docs | 5.3 |

Low-priority items intentionally deferred (note in session log if skipped): `security-crypto` deprecation migration (large, no active exploit — track separately), convention-plugin refactor (nice-to-have), LIKE wildcard `ESCAPE` (cosmetic). Raise these as future work rather than forcing them into this remediation.
