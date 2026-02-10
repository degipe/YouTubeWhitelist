package io.github.degipe.youtubewhitelist.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.degipe.youtubewhitelist.core.database.YouTubeWhitelistDatabase
import io.github.degipe.youtubewhitelist.core.database.dao.CachedChannelVideoDao
import io.github.degipe.youtubewhitelist.core.database.dao.KidProfileDao
import io.github.degipe.youtubewhitelist.core.database.dao.ParentAccountDao
import io.github.degipe.youtubewhitelist.core.database.dao.WatchHistoryDao
import io.github.degipe.youtubewhitelist.core.database.dao.WhitelistItemDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): YouTubeWhitelistDatabase {
        return Room.databaseBuilder(
            context,
            YouTubeWhitelistDatabase::class.java,
            "youtubewhitelist.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideParentAccountDao(db: YouTubeWhitelistDatabase): ParentAccountDao = db.parentAccountDao()

    @Provides
    fun provideKidProfileDao(db: YouTubeWhitelistDatabase): KidProfileDao = db.kidProfileDao()

    @Provides
    fun provideWhitelistItemDao(db: YouTubeWhitelistDatabase): WhitelistItemDao = db.whitelistItemDao()

    @Provides
    fun provideWatchHistoryDao(db: YouTubeWhitelistDatabase): WatchHistoryDao = db.watchHistoryDao()

    @Provides
    fun provideCachedChannelVideoDao(db: YouTubeWhitelistDatabase): CachedChannelVideoDao = db.cachedChannelVideoDao()
}
