package io.github.degipe.youtubewhitelist.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.degipe.youtubewhitelist.core.database.entity.ParentAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParentAccountDao {

    @Query("SELECT * FROM parent_accounts LIMIT 1")
    fun getParentAccount(): Flow<ParentAccountEntity?>

    @Query("SELECT * FROM parent_accounts LIMIT 1")
    suspend fun getParentAccountOnce(): ParentAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: ParentAccountEntity)

    @Update
    suspend fun update(account: ParentAccountEntity)

    @Query("DELETE FROM parent_accounts")
    suspend fun deleteAll()
}
