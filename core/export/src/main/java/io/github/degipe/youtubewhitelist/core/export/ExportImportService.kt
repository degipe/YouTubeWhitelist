package io.github.degipe.youtubewhitelist.core.export

import io.github.degipe.youtubewhitelist.core.common.result.AppResult

enum class ImportStrategy { MERGE, OVERWRITE }

data class ImportResult(
    val profilesImported: Int,
    val itemsImported: Int,
    val itemsSkipped: Int
)

interface ExportImportService {
    suspend fun exportToJson(parentAccountId: String): AppResult<String>
    suspend fun importFromJson(
        parentAccountId: String,
        json: String,
        strategy: ImportStrategy
    ): AppResult<ImportResult>
}
