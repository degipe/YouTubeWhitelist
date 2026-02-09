package io.github.degipe.youtubewhitelist.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType
import io.github.degipe.youtubewhitelist.core.database.entity.WhitelistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistItemDao {

    @Query("SELECT * FROM whitelist_items WHERE kidProfileId = :profileId ORDER BY addedAt DESC")
    fun getItemsByProfile(profileId: String): Flow<List<WhitelistItemEntity>>

    @Query("SELECT * FROM whitelist_items WHERE kidProfileId = :profileId AND type = :type ORDER BY addedAt DESC")
    fun getItemsByProfileAndType(profileId: String, type: WhitelistItemType): Flow<List<WhitelistItemEntity>>

    @Query("SELECT * FROM whitelist_items WHERE kidProfileId = :profileId AND youtubeId = :youtubeId LIMIT 1")
    suspend fun findByYoutubeId(profileId: String, youtubeId: String): WhitelistItemEntity?

    @Query("SELECT COUNT(*) FROM whitelist_items WHERE kidProfileId = :profileId")
    suspend fun getItemCount(profileId: String): Int

    @Query("SELECT youtubeId FROM whitelist_items WHERE kidProfileId = :profileId AND type = :type")
    suspend fun getYoutubeIdsByType(profileId: String, type: WhitelistItemType): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WhitelistItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WhitelistItemEntity>)

    @Delete
    suspend fun delete(item: WhitelistItemEntity)

    @Query("DELETE FROM whitelist_items WHERE kidProfileId = :profileId")
    suspend fun deleteAllByProfile(profileId: String)
}
