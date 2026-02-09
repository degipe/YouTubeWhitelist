package io.github.degipe.youtubewhitelist.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "kid_profiles",
    foreignKeys = [
        ForeignKey(
            entity = ParentAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentAccountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parentAccountId")]
)
data class KidProfileEntity(
    @PrimaryKey
    val id: String,
    val parentAccountId: String,
    val name: String,
    val avatarUrl: String? = null,
    val dailyLimitMinutes: Int? = null,
    val sleepPlaylistId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
