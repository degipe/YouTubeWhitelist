package io.github.degipe.youtubewhitelist.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.degipe.youtubewhitelist.core.database.dao.CachedChannelVideoDao
import io.github.degipe.youtubewhitelist.core.database.dao.KidProfileDao
import io.github.degipe.youtubewhitelist.core.database.dao.ParentAccountDao
import io.github.degipe.youtubewhitelist.core.database.dao.WatchHistoryDao
import io.github.degipe.youtubewhitelist.core.database.dao.WhitelistItemDao
import io.github.degipe.youtubewhitelist.core.database.entity.CachedChannelVideoEntity
import io.github.degipe.youtubewhitelist.core.database.entity.KidProfileEntity
import io.github.degipe.youtubewhitelist.core.database.entity.ParentAccountEntity
import io.github.degipe.youtubewhitelist.core.database.entity.WatchHistoryEntity
import io.github.degipe.youtubewhitelist.core.database.entity.WhitelistItemEntity

@Database(
    entities = [
        ParentAccountEntity::class,
        KidProfileEntity::class,
        WhitelistItemEntity::class,
        WatchHistoryEntity::class,
        CachedChannelVideoEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class YouTubeWhitelistDatabase : RoomDatabase() {
    abstract fun parentAccountDao(): ParentAccountDao
    abstract fun kidProfileDao(): KidProfileDao
    abstract fun whitelistItemDao(): WhitelistItemDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun cachedChannelVideoDao(): CachedChannelVideoDao
}
