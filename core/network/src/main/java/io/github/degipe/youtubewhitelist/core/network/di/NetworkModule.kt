package io.github.degipe.youtubewhitelist.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.degipe.youtubewhitelist.core.network.api.YouTubeApiService
import io.github.degipe.youtubewhitelist.core.network.interceptor.ApiKeyInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(@YouTubeApiKey apiKey: String): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(apiKey))
        // Only log in debug builds. Note: API key is in URL query params,
        // so logging must be disabled in release to avoid leaking it.
        if (io.github.degipe.youtubewhitelist.core.network.BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideYouTubeApiService(retrofit: Retrofit): YouTubeApiService =
        retrofit.create(YouTubeApiService::class.java)
}
