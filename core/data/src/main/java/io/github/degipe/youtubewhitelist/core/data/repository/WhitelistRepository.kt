package io.github.degipe.youtubewhitelist.core.data.repository

import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem
import kotlinx.coroutines.flow.Flow

interface WhitelistRepository {
    fun getItemsByProfile(profileId: String): Flow<List<WhitelistItem>>
    fun getItemsByProfileAndType(profileId: String, type: WhitelistItemType): Flow<List<WhitelistItem>>
    suspend fun addItemFromUrl(profileId: String, url: String): AppResult<WhitelistItem>
    suspend fun removeItem(item: WhitelistItem)
    suspend fun isAlreadyWhitelisted(profileId: String, youtubeId: String): Boolean
    suspend fun getItemCount(profileId: String): Int

    // Kid mode queries
    fun getChannelsByProfile(profileId: String): Flow<List<WhitelistItem>>
    fun getVideosByProfile(profileId: String): Flow<List<WhitelistItem>>
    fun getPlaylistsByProfile(profileId: String): Flow<List<WhitelistItem>>
    fun getVideosByChannelTitle(profileId: String, channelTitle: String): Flow<List<WhitelistItem>>
    fun searchItems(profileId: String, query: String): Flow<List<WhitelistItem>>
    fun getItemById(itemId: String): Flow<WhitelistItem?>
}
