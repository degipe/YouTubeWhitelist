package io.github.degipe.youtubewhitelist.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.degipe.youtubewhitelist.BuildConfig
import io.github.degipe.youtubewhitelist.core.auth.di.GoogleClientId
import io.github.degipe.youtubewhitelist.core.auth.di.GoogleClientSecret
import io.github.degipe.youtubewhitelist.core.network.di.YouTubeApiKey

@Module
@InstallIn(SingletonComponent::class)
object ApiKeyModule {

    @Provides
    @YouTubeApiKey
    fun provideYouTubeApiKey(): String = BuildConfig.YOUTUBE_API_KEY

    @Provides
    @GoogleClientId
    fun provideGoogleClientId(): String = BuildConfig.GOOGLE_CLIENT_ID

    @Provides
    @GoogleClientSecret
    fun provideGoogleClientSecret(): String = BuildConfig.GOOGLE_CLIENT_SECRET
}
