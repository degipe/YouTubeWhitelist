package io.github.degipe.youtubewhitelist.core.auth.repository

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.auth.google.GoogleSignInManager
import io.github.degipe.youtubewhitelist.core.auth.google.GoogleSignInResult
import io.github.degipe.youtubewhitelist.core.auth.token.TokenManager
import io.github.degipe.youtubewhitelist.core.data.model.AuthState
import io.github.degipe.youtubewhitelist.core.database.dao.ParentAccountDao
import io.github.degipe.youtubewhitelist.core.database.entity.ParentAccountEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var googleSignInManager: GoogleSignInManager
    private lateinit var tokenManager: TokenManager
    private lateinit var parentAccountDao: ParentAccountDao
    private lateinit var repository: AuthRepositoryImpl
    private lateinit var mockContext: Context
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        googleSignInManager = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)
        parentAccountDao = mockk(relaxed = true)
        mockContext = mockk()

        repository = AuthRepositoryImpl(
            googleSignInManager = googleSignInManager,
            tokenManager = tokenManager,
            parentAccountDao = parentAccountDao,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `signIn saves tokens and creates account on success`() = runTest(testDispatcher) {
        coEvery { googleSignInManager.signIn(any()) } returns GoogleSignInResult.Success(
            googleAccountId = "google-123",
            email = "test@example.com",
            displayName = "Test User",
            accessToken = "access-token",
            refreshToken = "refresh-token"
        )
        val accountSlot = slot<ParentAccountEntity>()
        coEvery { parentAccountDao.insert(capture(accountSlot)) } returns Unit

        repository.signIn(mockContext)

        coVerify { tokenManager.saveTokens("access-token", "refresh-token", any()) }
        assertThat(accountSlot.captured.googleAccountId).isEqualTo("google-123")
        assertThat(accountSlot.captured.email).isEqualTo("test@example.com")
        assertThat(accountSlot.captured.pinHash).isEmpty()
        assertThat(repository.authState.value).isInstanceOf(AuthState.Authenticated::class.java)
    }

    @Test(expected = Exception::class)
    fun `signIn throws on google sign in error`() = runTest(testDispatcher) {
        coEvery { googleSignInManager.signIn(any()) } returns GoogleSignInResult.Error(
            message = "Sign in failed",
            exception = RuntimeException("Network error")
        )

        repository.signIn(mockContext)
    }

    @Test(expected = Exception::class)
    fun `signIn throws on cancellation`() = runTest(testDispatcher) {
        coEvery { googleSignInManager.signIn(any()) } returns GoogleSignInResult.Cancelled

        repository.signIn(mockContext)
    }

    @Test
    fun `signOut clears tokens and updates state`() = runTest(testDispatcher) {
        repository.signOut()

        coVerify { tokenManager.clearTokens() }
        coVerify { googleSignInManager.signOut() }
        coVerify { parentAccountDao.deleteAll() }
        assertThat(repository.authState.value).isEqualTo(AuthState.Unauthenticated)
    }

    @Test
    fun `checkAuthState sets Authenticated when account exists`() = runTest(testDispatcher) {
        val entity = ParentAccountEntity(
            id = "test-id",
            googleAccountId = "google-id",
            email = "test@example.com",
            pinHash = "salt:hash",
            createdAt = 1000L
        )
        coEvery { parentAccountDao.getParentAccountOnce() } returns entity

        repository.checkAuthState()

        assertThat(repository.authState.value).isInstanceOf(AuthState.Authenticated::class.java)
    }

    @Test
    fun `checkAuthState sets Unauthenticated when no account`() = runTest(testDispatcher) {
        coEvery { parentAccountDao.getParentAccountOnce() } returns null

        repository.checkAuthState()

        assertThat(repository.authState.value).isEqualTo(AuthState.Unauthenticated)
    }

    @Test
    fun `initial state is Loading`() {
        assertThat(repository.authState.value).isEqualTo(AuthState.Loading)
    }
}
