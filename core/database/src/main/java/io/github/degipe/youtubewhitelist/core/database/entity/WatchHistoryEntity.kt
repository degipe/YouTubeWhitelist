package io.github.degipe.youtubewhitelist.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watch_history",
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
        Index(value = ["kidProfileId", "watchedAt"])
    ]
)
data class WatchHistoryEntity(
    @PrimaryKey
    val id: String,
    val kidProfileId: String,
    val videoId: String,
    val videoTitle: String,
    val watchedSeconds: Int,
    val watchedAt: Long = System.currentTimeMillis()
)
