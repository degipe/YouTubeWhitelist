package io.github.degipe.youtubewhitelist.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.WatchHistoryRepository
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import io.github.degipe.youtubewhitelist.core.data.repository.YouTubeApiRepository
import io.github.degipe.youtubewhitelist.core.data.repository.impl.KidProfileRepositoryImpl
import io.github.degipe.youtubewhitelist.core.data.repository.impl.WatchHistoryRepositoryImpl
import io.github.degipe.youtubewhitelist.core.data.repository.impl.WhitelistRepositoryImpl
import io.github.degipe.youtubewhitelist.core.data.repository.impl.HybridYouTubeRepositoryImpl
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerManager
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerManagerImpl
import io.github.degipe.youtubewhitelist.core.data.timelimit.TimeLimitChecker
import io.github.degipe.youtubewhitelist.core.data.timelimit.TimeLimitCheckerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindYouTubeApiRepository(impl: HybridYouTubeRepositoryImpl): YouTubeApiRepository

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

    companion object {
        @Provides
        @Singleton
        fun provideSleepTimerManager(): SleepTimerManager =
            SleepTimerManagerImpl(CoroutineScope(SupervisorJob() + Dispatchers.Default))
    }
}
