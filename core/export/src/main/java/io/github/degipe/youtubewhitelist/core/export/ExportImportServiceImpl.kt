package io.github.degipe.youtubewhitelist.core.export

import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.common.youtube.YouTubeId
import io.github.degipe.youtubewhitelist.core.database.YouTubeWhitelistDatabase
import io.github.degipe.youtubewhitelist.core.database.dao.KidProfileDao
import io.github.degipe.youtubewhitelist.core.database.dao.WhitelistItemDao
import io.github.degipe.youtubewhitelist.core.database.entity.KidProfileEntity
import io.github.degipe.youtubewhitelist.core.database.entity.WhitelistItemEntity
import io.github.degipe.youtubewhitelist.core.export.model.ExportData
import io.github.degipe.youtubewhitelist.core.export.model.ExportProfile
import io.github.degipe.youtubewhitelist.core.export.model.ExportWhitelistItem
import androidx.room.withTransaction
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportImportServiceImpl @Inject constructor(
    private val kidProfileDao: KidProfileDao,
    private val whitelistItemDao: WhitelistItemDao,
    private val database: YouTubeWhitelistDatabase
) : ExportImportService {

    private val json = Json { prettyPrint = true }

    override suspend fun exportToJson(parentAccountId: String): AppResult<String> {
        return try {
            val profiles = kidProfileDao.getProfilesByParent(parentAccountId).first()
            val exportProfiles = profiles.map { profile ->
                val items = whitelistItemDao.getItemsByProfile(profile.id).first()
                ExportProfile(
                    name = profile.name,
                    avatarUrl = profile.avatarUrl,
                    dailyLimitMinutes = profile.dailyLimitMinutes,
                    sleepPlaylistId = profile.sleepPlaylistId,
                    whitelistItems = items.map { item ->
                        ExportWhitelistItem(
                            type = item.type.name,
                            youtubeId = item.youtubeId,
                            title = item.title,
                            thumbnailUrl = item.thumbnailUrl,
                            channelTitle = item.channelTitle
                        )
                    }
                )
            }

            val exportData = ExportData(
                version = 1,
                exportedAt = System.currentTimeMillis(),
                profiles = exportProfiles
            )

            AppResult.Success(json.encodeToString(ExportData.serializer(), exportData))
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Export failed")
        }
    }

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
                    } else {
                        null
                    }

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

                        // WhitelistItemType.valueOf() may throw for an unknown type - that is
                        // intentionally NOT caught here, it aborts the whole import (rolled back
                        // by the surrounding transaction), matching the pre-existing behavior.
                        val type = WhitelistItemType.valueOf(exportItem.type)

                        // B1: reject IDs that don't match YouTube's known ID formats before they
                        // can reach the DB. An unvalidated ID would later be string-interpolated
                        // into the kid player's WebView JavaScript, allowing a crafted import
                        // .json to break out of the JS string literal and execute arbitrary script.
                        val isValidId = when (type) {
                            WhitelistItemType.VIDEO -> YouTubeId.isValidVideoId(exportItem.youtubeId)
                            WhitelistItemType.CHANNEL -> YouTubeId.isValidChannelId(exportItem.youtubeId)
                            WhitelistItemType.PLAYLIST -> YouTubeId.isValidPlaylistId(exportItem.youtubeId)
                        }
                        if (!isValidId) {
                            itemsSkipped++
                            continue
                        }

                        whitelistItemDao.insert(
                            WhitelistItemEntity(
                                id = UUID.randomUUID().toString(),
                                kidProfileId = profileId,
                                type = type,
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
}
