package io.github.degipe.youtubewhitelist.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.degipe.youtubewhitelist.core.common.model.WhitelistItemType

@Entity(
    tableName = "whitelist_items",
    foreignKeys = [
        ForeignKey(
            entity = KidProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["kidProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("kidProfileId"),
        Index(value = ["kidProfileId", "type"]),
        Index(value = ["kidProfileId", "youtubeId"], unique = true)
    ]
)
data class WhitelistItemEntity(
    @PrimaryKey
    val id: String,
    val kidProfileId: String,
    val type: WhitelistItemType,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val channelTitle: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)
