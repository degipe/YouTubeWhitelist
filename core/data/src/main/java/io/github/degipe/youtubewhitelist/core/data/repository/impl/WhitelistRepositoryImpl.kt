package io.github.degipe.youtubewhitelist.core.data.repository.impl

import io.github.degipe.youtubewhitelist.core.common.di.IoDispatcher
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.common.youtube.YouTubeContentType
import io.github.degipe.youtubewhitelist.core.common.youtube.YouTubeUrlParser
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import io.github.degipe.youtubewhitelist.core.data.model.YouTubeMetadata
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import io.github.degipe.youtubewhitelist.core.database.dao.WhitelistItemDao
import io.github.degipe.youtubewhitelist.core.database.entity.WhitelistItemEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class WhitelistRepositoryImpl @Inject constructor(
    private val whitelistItemDao: WhitelistItemDao,
    private val youTubeApiRepository: YouTubeApiRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : WhitelistRepository {

    override fun getItemsByProfile(profileId: String): Flow<List<WhitelistItem>> =
        whitelistItemDao.getItemsByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getItemsByProfileAndType(
        profileId: String,
        type: WhitelistItemType
    ): Flow<List<WhitelistItem>> =
        whitelistItemDao.getItemsByProfileAndType(profileId, type).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addItemFromUrl(
        profileId: String,
        url: String
    ): AppResult<WhitelistItem> = withContext(ioDispatcher) {
        val parsed = YouTubeUrlParser.parse(url)
            ?: return@withContext AppResult.Error("Invalid YouTube URL")

        // For types where the ID is already known (VIDEO, CHANNEL, PLAYLIST),
        // check for duplicates early to avoid wasting an API quota call.
        val knownId = when (parsed.type) {
            YouTubeContentType.VIDEO,
            YouTubeContentType.CHANNEL,
            YouTubeContentType.PLAYLIST -> parsed.id
            // CHANNEL_HANDLE and CHANNEL_CUSTOM require API resolution first
            else -> null
        }
        if (knownId != null && whitelistItemDao.findByYoutubeId(profileId, knownId) != null) {
            return@withContext AppResult.Error("Already whitelisted")
        }

        // Resolve type and fetch metadata via YouTube API
        val resolved = resolveContent(parsed.type, parsed.id)
        when (resolved) {
            is AppResult.Error -> return@withContext resolved
            is AppResult.Success -> { /* continue */ }
        }
        val (itemType, metadata) = resolved.data

        // Check duplicate for resolved IDs (handles/custom URLs resolve to channel IDs)
        if (knownId == null && whitelistItemDao.findByYoutubeId(profileId, metadata.youtubeId) != null) {
            return@withContext AppResult.Error("Already whitelisted")
        }

        // Create and store
        val item = WhitelistItem(
            id = UUID.randomUUID().toString(),
            kidProfileId = profileId,
            type = itemType,
            youtubeId = metadata.youtubeId,
            title = metadata.title,
            thumbnailUrl = metadata.thumbnailUrl,
            channelTitle = when (metadata) {
                is YouTubeMetadata.Video -> metadata.channelTitle
                is YouTubeMetadata.Playlist -> metadata.channelTitle
                is YouTubeMetadata.Channel -> null
            },
            addedAt = System.currentTimeMillis()
        )
        whitelistItemDao.insert(item.toEntity())
        AppResult.Success(item)
    }

    override suspend fun removeItem(item: WhitelistItem) = withContext(ioDispatcher) {
        whitelistItemDao.delete(item.toEntity())
    }

    override suspend fun isAlreadyWhitelisted(profileId: String, youtubeId: String): Boolean =
        withContext(ioDispatcher) {
            whitelistItemDao.findByYoutubeId(profileId, youtubeId) != null
        }

    override suspend fun getItemCount(profileId: String): Int =
        withContext(ioDispatcher) {
            whitelistItemDao.getItemCount(profileId)
        }

    private suspend fun resolveContent(
        contentType: YouTubeContentType,
        id: String
    ): AppResult<Pair<WhitelistItemType, YouTubeMetadata>> {
        return when (contentType) {
            YouTubeContentType.VIDEO -> {
                when (val result = youTubeApiRepository.getVideoById(id)) {
                    is AppResult.Success -> AppResult.Success(WhitelistItemType.VIDEO to result.data)
                    is AppResult.Error -> result
                }
            }
            YouTubeContentType.CHANNEL -> {
                when (val result = youTubeApiRepository.getChannelById(id)) {
                    is AppResult.Success -> AppResult.Success(WhitelistItemType.CHANNEL to result.data)
                    is AppResult.Error -> result
                }
            }
            YouTubeContentType.CHANNEL_HANDLE -> {
                when (val result = youTubeApiRepository.getChannelByHandle(id)) {
                    is AppResult.Success -> AppResult.Success(WhitelistItemType.CHANNEL to result.data)
                    is AppResult.Error -> result
                }
            }
            YouTubeContentType.CHANNEL_CUSTOM -> {
                // Legacy /c/CustomName URLs: try forHandle resolution.
                // Modern YouTube maps custom URLs to @handles, so this usually works.
                // If forHandle fails, future improvement could use search.list as fallback.
                when (val result = youTubeApiRepository.getChannelByHandle(id)) {
                    is AppResult.Success -> AppResult.Success(WhitelistItemType.CHANNEL to result.data)
                    is AppResult.Error -> result
                }
            }
            YouTubeContentType.PLAYLIST -> {
                when (val result = youTubeApiRepository.getPlaylistById(id)) {
                    is AppResult.Success -> AppResult.Success(WhitelistItemType.PLAYLIST to result.data)
                    is AppResult.Error -> result
                }
            }
        }
    }

    // Kid mode queries
    override fun getChannelsByProfile(profileId: String): Flow<List<WhitelistItem>> =
        whitelistItemDao.getChannelsByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getVideosByProfile(profileId: String): Flow<List<WhitelistItem>> =
        whitelistItemDao.getVideosByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getPlaylistsByProfile(profileId: String): Flow<List<WhitelistItem>> =
        whitelistItemDao.getPlaylistsByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getVideosByChannelTitle(profileId: String, channelTitle: String): Flow<List<WhitelistItem>> =
        whitelistItemDao.getVideosByChannelTitle(profileId, channelTitle).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun searchItems(profileId: String, query: String): Flow<List<WhitelistItem>> =
        whitelistItemDao.searchItems(profileId, query).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getItemById(itemId: String): Flow<WhitelistItem?> =
        whitelistItemDao.getItemById(itemId).map { it?.toDomain() }

    companion object {
        fun WhitelistItemEntity.toDomain(): WhitelistItem = WhitelistItem(
            id = id,
            kidProfileId = kidProfileId,
            type = type,
            youtubeId = youtubeId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            channelTitle = channelTitle,
            addedAt = addedAt
        )

        fun WhitelistItem.toEntity(): WhitelistItemEntity = WhitelistItemEntity(
            id = id,
            kidProfileId = kidProfileId,
            type = type,
            youtubeId = youtubeId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            channelTitle = channelTitle,
            addedAt = addedAt
        )
    }
}
