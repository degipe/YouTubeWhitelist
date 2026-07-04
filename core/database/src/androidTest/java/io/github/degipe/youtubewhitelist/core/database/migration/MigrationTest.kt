package io.github.degipe.youtubewhitelist.core.database.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.degipe.youtubewhitelist.core.database.YouTubeWhitelistDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented migration tests. Runs against a device/emulator via `connectedAndroidTest`.
 *
 * Template for future migrations: when a new Migration (e.g. MIGRATION_3_4) is added to
 * [Migrations.ALL], add a corresponding test here that inserts data at the old version,
 * runs the migration, and asserts the data survived (see Room's official migration test guide).
 */
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
