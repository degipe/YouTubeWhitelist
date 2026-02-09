package io.github.degipe.youtubewhitelist.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.WatchHistoryRepository
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import io.github.degipe.youtubewhitelist.core.data.repository.impl.KidProfileRepositoryImpl
import io.github.degipe.youtubewhitelist.core.data.repository.impl.WatchHistoryRepositoryImpl
import io.github.degipe.youtubewhitelist.core.data.repository.impl.WhitelistRepositoryImpl
import io.github.degipe.youtubewhitelist.core.data.repository.impl.YouTubeApiRepositoryImpl
import io.github.degipe.youtubewhitelist.core.data.timelimit.TimeLimitChecker
import io.github.degipe.youtubewhitelist.core.data.timelimit.TimeLimitCheckerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindYouTubeApiRepository(impl: YouTubeApiRepositoryImpl): YouTubeApiRepository

    @Binds
    @Singleton
    abstract fun bindWhitelistRepository(impl: WhitelistRepositoryImpl): WhitelistRepository

    @Binds
    @Singleton
    abstract fun bindKidProfileRepository(impl: KidProfileRepositoryImpl): KidProfileRepository

    @Binds
    @Singleton
    abstract fun bindWatchHistoryRepository(impl: WatchHistoryRepositoryImpl): WatchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindTimeLimitChecker(impl: TimeLimitCheckerImpl): TimeLimitChecker
}
