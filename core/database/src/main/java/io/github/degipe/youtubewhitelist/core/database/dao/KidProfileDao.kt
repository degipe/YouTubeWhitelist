package io.github.degipe.youtubewhitelist.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.degipe.youtubewhitelist.core.database.entity.KidProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KidProfileDao {

    @Query("SELECT * FROM kid_profiles WHERE parentAccountId = :parentId ORDER BY createdAt ASC")
    fun getProfilesByParent(parentId: String): Flow<List<KidProfileEntity>>

    @Query("SELECT * FROM kid_profiles WHERE id = :profileId")
    fun getProfileById(profileId: String): Flow<KidProfileEntity?>

    @Query("SELECT * FROM kid_profiles WHERE id = :profileId")
    suspend fun getProfileByIdOnce(profileId: String): KidProfileEntity?

    @Query("SELECT COUNT(*) FROM kid_profiles WHERE parentAccountId = :parentId")
    suspend fun getProfileCount(parentId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: KidProfileEntity)

    @Update
    suspend fun update(profile: KidProfileEntity)

    @Delete
    suspend fun delete(profile: KidProfileEntity)
}
