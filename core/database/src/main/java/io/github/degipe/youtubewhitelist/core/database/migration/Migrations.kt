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
