package io.github.degipe.youtubewhitelist.core.export

import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.database.dao.KidProfileDao
import io.github.degipe.youtubewhitelist.core.database.dao.WhitelistItemDao
import io.github.degipe.youtubewhitelist.core.database.entity.KidProfileEntity
import io.github.degipe.youtubewhitelist.core.database.entity.WhitelistItemEntity
import io.github.degipe.youtubewhitelist.core.export.model.ExportData
import io.github.degipe.youtubewhitelist.core.export.model.ExportProfile
import io.github.degipe.youtubewhitelist.core.export.model.ExportWhitelistItem
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportImportServiceImpl @Inject constructor(
    private val kidProfileDao: KidProfileDao,
    private val whitelistItemDao: WhitelistItemDao
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

            if (strategy == ImportStrategy.OVERWRITE) {
                val existingProfiles = kidProfileDao.getProfilesByParent(parentAccountId).first()
                existingProfiles.forEach { kidProfileDao.delete(it) }
            }

            var totalProfilesImported = 0
            var totalItemsImported = 0
            var totalItemsSkipped = 0

            for (exportProfile in exportData.profiles) {
                val newProfileId = UUID.randomUUID().toString()
                val profileEntity = KidProfileEntity(
                    id = newProfileId,
                    parentAccountId = parentAccountId,
                    name = exportProfile.name,
                    avatarUrl = exportProfile.avatarUrl,
                    dailyLimitMinutes = exportProfile.dailyLimitMinutes,
                    sleepPlaylistId = exportProfile.sleepPlaylistId
                )
                kidProfileDao.insert(profileEntity)
                totalProfilesImported++

                for (exportItem in exportProfile.whitelistItems) {
                    if (strategy == ImportStrategy.MERGE) {
                        val existing = whitelistItemDao.findByYoutubeId(newProfileId, exportItem.youtubeId)
                        if (existing != null) {
                            totalItemsSkipped++
                            continue
                        }
                    }

                    val itemEntity = WhitelistItemEntity(
                        id = UUID.randomUUID().toString(),
                        kidProfileId = newProfileId,
                        type = WhitelistItemType.valueOf(exportItem.type),
                        youtubeId = exportItem.youtubeId,
                        title = exportItem.title,
                        thumbnailUrl = exportItem.thumbnailUrl,
                        channelTitle = exportItem.channelTitle
                    )
                    whitelistItemDao.insert(itemEntity)
                    totalItemsImported++
                }
            }

            AppResult.Success(
                ImportResult(
                    profilesImported = totalProfilesImported,
                    itemsImported = totalItemsImported,
                    itemsSkipped = totalItemsSkipped
                )
            )
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Import failed")
        }
    }
}
