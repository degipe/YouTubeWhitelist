package io.github.degipe.youtubewhitelist.core.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "cached_channel_videos",
    primaryKeys = ["channelId", "videoId"],
    indices = [
        Index("channelId")
    ]
)
data class CachedChannelVideoEntity(
    val channelId: String,
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val channelTitle: String,
    val position: Int,
    val cachedAt: Long = System.currentTimeMillis()
)
