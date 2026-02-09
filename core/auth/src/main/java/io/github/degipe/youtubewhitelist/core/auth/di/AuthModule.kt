package io.github.degipe.youtubewhitelist.core.auth.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.degipe.youtubewhitelist.core.auth.google.GoogleSignInManager
import io.github.degipe.youtubewhitelist.core.auth.google.GoogleSignInManagerImpl
import io.github.degipe.youtubewhitelist.core.auth.pin.BruteForceProtection
import io.github.degipe.youtubewhitelist.core.auth.pin.Pbkdf2PinHasher
import io.github.degipe.youtubewhitelist.core.auth.pin.PinHasher
import io.github.degipe.youtubewhitelist.core.auth.pin.PinRepositoryImpl
import io.github.degipe.youtubewhitelist.core.auth.repository.AuthRepositoryImpl
import io.github.degipe.youtubewhitelist.core.auth.repository.ParentAccountRepositoryImpl
import io.github.degipe.youtubewhitelist.core.auth.token.EncryptedTokenManager
import io.github.degipe.youtubewhitelist.core.auth.token.TokenManager
import io.github.degipe.youtubewhitelist.core.data.repository.AuthRepository
import io.github.degipe.youtubewhitelist.core.data.repository.ParentAccountRepository
import io.github.degipe.youtubewhitelist.core.data.repository.PinRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPinRepository(impl: PinRepositoryImpl): PinRepository

    @Binds
    @Singleton
    abstract fun bindParentAccountRepository(impl: ParentAccountRepositoryImpl): ParentAccountRepository

    @Binds
    @Singleton
    abstract fun bindTokenManager(impl: EncryptedTokenManager): TokenManager

    @Binds
    @Singleton
    abstract fun bindPinHasher(impl: Pbkdf2PinHasher): PinHasher

    @Binds
    @Singleton
    abstract fun bindGoogleSignInManager(impl: GoogleSignInManagerImpl): GoogleSignInManager

    companion object {
        @Provides
        @Singleton
        fun provideBruteForceProtection(
            @ApplicationContext context: Context
        ): BruteForceProtection {
            val prefs = context.getSharedPreferences("pin_brute_force", Context.MODE_PRIVATE)
            return BruteForceProtection(prefs)
        }
    }
}
