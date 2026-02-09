package io.github.degipe.youtubewhitelist.core.auth.repository

import android.content.Context
import io.github.degipe.youtubewhitelist.core.auth.google.GoogleSignInManager
import io.github.degipe.youtubewhitelist.core.auth.google.GoogleSignInResult
import io.github.degipe.youtubewhitelist.core.auth.repository.ParentAccountRepositoryImpl.Companion.toDomain
import io.github.degipe.youtubewhitelist.core.auth.token.TokenManager
import io.github.degipe.youtubewhitelist.core.common.di.IoDispatcher
import io.github.degipe.youtubewhitelist.core.data.model.AuthState
import io.github.degipe.youtubewhitelist.core.data.repository.AuthRepository
import io.github.degipe.youtubewhitelist.core.database.dao.ParentAccountDao
import io.github.degipe.youtubewhitelist.core.database.entity.ParentAccountEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val googleSignInManager: GoogleSignInManager,
    private val tokenManager: TokenManager,
    private val parentAccountDao: ParentAccountDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun signIn(activityContext: Context) = withContext(ioDispatcher) {
        val result = googleSignInManager.signIn(activityContext)
        when (result) {
            is GoogleSignInResult.Success -> {
                tokenManager.saveTokens(
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    expiresAt = System.currentTimeMillis() + TOKEN_EXPIRY_MS
                )
                // Reuse existing account if present, to avoid cascade deletion of kid profiles
                val existingAccount = parentAccountDao.getParentAccountOnce()
                val account = if (existingAccount != null) {
                    existingAccount.copy(
                        googleAccountId = result.googleAccountId,
                        email = result.email
                    ).also { parentAccountDao.update(it) }
                } else {
                    ParentAccountEntity(
                        id = UUID.randomUUID().toString(),
                        googleAccountId = result.googleAccountId,
                        email = result.email,
                        pinHash = "",
                        createdAt = System.currentTimeMillis()
                    ).also { parentAccountDao.insert(it) }
                }
                _authState.value = AuthState.Authenticated(account.toDomain())
            }
            is GoogleSignInResult.Error -> {
                _authState.value = AuthState.Unauthenticated
                throw Exception(result.message, result.exception)
            }
            is GoogleSignInResult.Cancelled -> {
                _authState.value = AuthState.Unauthenticated
                throw Exception("Sign-in cancelled")
            }
        }
    }

    override suspend fun signOut() = withContext(ioDispatcher) {
        tokenManager.clearTokens()
        googleSignInManager.signOut()
        parentAccountDao.deleteAll()
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun checkAuthState() = withContext(ioDispatcher) {
        val account = parentAccountDao.getParentAccountOnce()
        _authState.value = if (account != null) {
            AuthState.Authenticated(account.toDomain())
        } else {
            AuthState.Unauthenticated
        }
    }

    companion object {
        private const val TOKEN_EXPIRY_MS = 3600_000L // 1 hour
    }
}
